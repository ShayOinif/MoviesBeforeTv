package com.shayo.moviespoint.viewmodels.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.shayo.movies.MovieManager
import com.shayo.movies.PagedItem
import com.shayo.moviespoint.getcategoriesflows.CategoryName
import com.shayo.moviespoint.getcategoriesflows.GetCategoriesFlowsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KClass

/* TODO: Right now the categories are refreshed only when the app is created, so for a next day refresh
 *  the app must be destroyed before a refresh would happen, fix by collecting with lifecycle and
 *  make the categories a flow instead of a simple list or, by registering a worker that refreshes
 *  the categories once a day.
 */

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieManager: MovieManager,
    getCategoriesFlowsUseCase: GetCategoriesFlowsUseCase,
) : ViewModel() {

    // TODO: Make internal once there are tests
    private val homeViewModelParamsFlow = MutableStateFlow<HomeViewModelParams<*>?>(null)

    fun <T: Any> setup(
        withGenres: Boolean,
        itemClass: KClass<T>,
        itemMapper: (pagedMovie: PagedItem.PagedMovie) ->  T,
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
                        flow = category.flow.map { pagedData ->
                            pagedData.map { pagedMovie ->
                                homeViewModelParams.itemMapper(pagedMovie)
                            }
                        }.cachedIn(viewModelScope)
                    )
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1_500), // TODO:
        initialValue = null
    )


        /*getCategoriesFlowsUseCase(viewModelScope, false)
        .map { category ->
            HomeCategory(
                nameRes = when (category.name) {
                    CategoryName.POPULAR_MOVIES -> R.string.popular_movies
                    CategoryName.UPCOMING_MOVIES -> R.string.upcoming_movies
                    CategoryName.POPULAR_SHOWS -> R.string.popular_shows
                    CategoryName.TOP_SHOWS -> R.string.top_shows
                },
                flow = category.flow.map { pagedData ->
                    pagedData.map { pagedMovie ->
                        with(pagedMovie.movie) {
                            HomeItem(
                                id, type,

                                MediaCardItem(
                                    posterPath,
                                    title,
                                    voteAverage.toString(),
                                    releaseDate,
                                    inWatchlist = isFavorite,
                                )
                            )
                        }
                    }
                }.cachedIn(viewModelScope)
            )
        }*/

    fun watchlistClick(id: Int, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            movieManager.toggleFavorite(id, type)
        }
    }
}

internal data class HomeViewModelParams<T: Any>(
    val itemClass: KClass<T>,
    val itemMapper: (pagedMovie: PagedItem.PagedMovie) ->  T,
    val withGenres: Boolean,
)

data class HomeCategory<T: Any>(
    @StringRes
    val nameRes: Int,
    val flow: Flow<PagingData<T>>
)