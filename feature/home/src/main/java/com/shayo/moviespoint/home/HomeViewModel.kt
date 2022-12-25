package com.shayo.moviespoint.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.shayo.movies.MovieManager
import com.shayo.moviespoint.getcategoriesflows.CategoryName
import com.shayo.moviespoint.getcategoriesflows.GetCategoriesFlowsUseCase
import com.shayo.moviespoint.ui.MediaCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val movieManager: MovieManager,
    getCategoriesFlowsUseCase: GetCategoriesFlowsUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoriesFlows = getCategoriesFlowsUseCase(viewModelScope, false)
        .map { category ->
            HomeCategory(
                nameRes = when (category.name) {
                    CategoryName.POPULAR_MOVIES -> R.string.popular_movies
                    CategoryName.UPCOMING_MOVIES -> R.string.upcoming_movies
                    CategoryName.POPULAR_SHOWS -> R.string.popular_shows
                    CategoryName.TOP_SHOWS -> R.string.top_shows
                },
                flow = category.flow.mapLatest { pagedData ->
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
                }
            )
        }

    fun watchlistClick(id: Int, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            movieManager.toggleFavorite(id, type)
        }
    }
}

internal data class HomeCategory(
    @StringRes
    val nameRes: Int,
    val flow: Flow<PagingData<HomeItem>>
)

internal data class HomeItem(
    val id: Int,
    val type: String,
    val mediaCardItem: MediaCardItem,
)