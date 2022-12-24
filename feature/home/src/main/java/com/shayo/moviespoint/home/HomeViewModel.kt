package com.shayo.moviespoint.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.map
import com.shayo.movies.MovieManager
import com.shayo.moviespoint.ui.MediaCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieManager: MovieManager,
) : ViewModel() {

    val moviesFlow = combine(
        movieManager.getCategoryFlow(
            type = "movie",
            category = "popular",
            scope = viewModelScope,
        ),
        movieManager.favoritesMap
    ) { page, favorites ->
        page.map { pagedMovie ->
            with (pagedMovie.movie) {
                MediaCardItem(
                    id,
                    posterPath,
                    title,
                    voteAverage.toString(),
                    releaseDate,
                    inWatchlist = favorites.containsKey(id),
                )
            }
        }
    }

    fun watchlistClick(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            movieManager.toggleFavorite(id, "movie")
        }
    }
}