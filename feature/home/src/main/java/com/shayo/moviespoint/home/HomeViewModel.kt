package com.shayo.moviespoint.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.shayo.movies.MovieManager
import com.shayo.moviespoint.getcategoriesflows.CategoryName
import com.shayo.moviespoint.getcategoriesflows.GetCategoriesFlowsUseCase
import com.shayo.moviespoint.ui.MediaCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/* TODO: Right now the categories are refreshed only when the app is created, so for a next day refresh
 *  the app must be destroyed before a refresh would happen, fix by collecting with lifecycle and
 *  make the categories a flow instead of a simple list or, by registering a worker that refreshes
 *  the categories once a day.
 */

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val movieManager: MovieManager,
    getCategoriesFlowsUseCase: GetCategoriesFlowsUseCase,
) : ViewModel() {

    val categoriesFlows = getCategoriesFlowsUseCase(viewModelScope, false)
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