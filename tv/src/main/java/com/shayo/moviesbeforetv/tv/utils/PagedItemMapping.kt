package com.shayo.moviesbeforetv.tv.utils

import com.shayo.movies.PagedItem
import com.shayo.moviesbeforetv.tv.BrowseMovieLoadResult

internal fun PagedItem.mapToBrowseResult(category: String? = null) =
    when (this) {
        is PagedItem.PagedMovie -> {
            BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
                movie,
                position,
                category
            )
        }
        is PagedItem.PagedCredit -> {
            BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit(
                credit,
                position
            )
        }
    }

