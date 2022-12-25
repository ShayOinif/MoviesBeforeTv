package com.shayo.moviespoint.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shayo.movies.MovieManager
import com.shayo.moviespoint.ui.MediaCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class WatchlistViewModel @Inject constructor(
    private val movieManager: MovieManager,
) : ViewModel() {

    val watchlistFlow = movieManager.getFavoritesFlow(withGenres = false).map { favorites ->
        favorites.map { result ->
            result.fold(
                onSuccess = { movie ->
                    with(movie) {
                        WatchlistItem(
                            id,
                            type,
                            MediaCardItem(
                                posterPath, title, voteAverage.toString(), releaseDate, true,
                            ),
                        )
                    }
                },
                onFailure = {
                    throw Exception() // TODO:
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1_500), // TODO:
        initialValue = emptyList(),
    )

    fun watchlistClick(id: Int, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            movieManager.toggleFavorite(id, type)
        }
    }
}

internal data class WatchlistItem(
    val id: Int,
    val type: String,
    val mediaCardItem: MediaCardItem,
)