package com.shayo.moviesbeforetv.tv.utils

import com.shayo.movies.PagedItem
import com.shayo.moviesbeforetv.tv.BrowseMovieLoadResult

// Test commit
internal fun PagedItem.mapToBrowseResult(favoritesMap: Map<Int, String>) =
    when (this) {
        is PagedItem.PagedMovie -> {
            BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
                movie,
                favoritesMap.containsKey(movie.id),
                position,
            )
        }
        is PagedItem.PagedCredit -> {
            BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit(
                credit,
                position
            )
        }
    }