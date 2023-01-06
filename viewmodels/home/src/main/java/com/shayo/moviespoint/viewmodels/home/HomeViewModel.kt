package com.shayo.moviespoint.viewmodels.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.shayo.movies.MovieManager
import com.shayo.movies.PagedItem
import com.shayo.moviespoint.data.usage.UsageRepository
import com.shayo.moviespoint.getcategoriesflows.CategoryName
import com.shayo.moviespoint.getcategoriesflows.GetCategoriesFlowsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieManager: MovieManager,
    getCategoriesFlowsUseCase: GetCategoriesFlowsUseCase,
    private val usageRepository: UsageRepository,
) : ViewModel() {

    // TODO: Make internal once there are tests
    private val homeViewModelParamsFlow = MutableStateFlow<HomeViewModelParams<*>?>(null)

    private val waitingUpdate = MutableStateFlow(mutableMapOf<Int, Unit?>())

    val shouldAskUsageFlow = usageRepository.shouldAskFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun markUsage(enabled: Boolean) {
        usageRepository.changeUsage(enabled)

        usageRepository.markAsked()
    }

    fun <T: Any> setup(
        withGenres: Boolean,
        itemClass: KClass<T>,
        itemMapper: (pagedMovie: PagedItem.PagedMovie, category: String, watchlistInProgress: Boolean) ->  T,
    ) {
        homeViewModelParamsFlow.value = HomeViewModelParams(itemClass, itemMapper, withGenres)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoriesFlows = homeViewModelParamsFlow.mapLatest { homeViewModelParams ->
        homeViewModelParams?.let  {
            getCategoriesFlowsUseCase(viewModelScope, false)
                .map { category ->
                    HomeCategory(
                        nameRes = when (category.name) {
                            CategoryName.POPULAR_MOVIES -> R.string.popular_movies
                            CategoryName.UPCOMING_MOVIES -> R.string.upcoming_movies
                            CategoryName.POPULAR_SHOWS -> R.string.popular_shows
                            CategoryName.TOP_SHOWS -> R.string.top_shows
                        },
                        name = category.name.category,
                        flow = combine(
                            category.flow,
                            waitingUpdate
                        ) { categoryFlow, waitingFlow ->
                            categoryFlow.map { pagedMovie ->
                                    homeViewModelParams.itemMapper(pagedMovie, category.name.category, waitingFlow.containsKey(pagedMovie.movie.id))
                            }
                        }.cachedIn(viewModelScope)
                    )
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    fun watchlistClick(id: Int, type: String) {
        waitingUpdate.update {
            val newMap = it.toMutableMap()
            newMap[id] = null
            newMap
        }

        viewModelScope.launch(Dispatchers.IO) {
            movieManager.toggleFavorite(id, type)

            waitingUpdate.update {
                val newMap = it.toMutableMap()
                newMap.remove(id)
                newMap
            }
        }
    }
}

internal data class HomeViewModelParams<T: Any>(
    val itemClass: KClass<T>,
    val itemMapper: (pagedMovie: PagedItem.PagedMovie, category: String, watchlistInProgress: Boolean) ->  T,
    val withGenres: Boolean,
)

data class HomeCategory<T: Any>(
    @StringRes
    val nameRes: Int,
    val name: String,
    val flow: Flow<PagingData<T>>
)