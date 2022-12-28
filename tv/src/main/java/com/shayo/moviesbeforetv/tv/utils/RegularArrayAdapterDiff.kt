package com.shayo.moviesbeforetv.tv.utils

import androidx.leanback.widget.DiffCallback
import com.shayo.moviesbeforetv.tv.BrowseMovieLoadResult

class RegularArrayAdapterDiff : DiffCallback<BrowseMovieLoadResult.BrowseMovieLoadSuccess>() {
    override fun areItemsTheSame(
        oldItem: BrowseMovieLoadResult.BrowseMovieLoadSuccess,
        newItem: BrowseMovieLoadResult.BrowseMovieLoadSuccess
    ): Boolean {
        return if (oldItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie &&
            newItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie
        ) {
            oldItem.movie.id == newItem.movie.id
        } else if (oldItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit &&
            newItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit
        ) {
            oldItem.credit.id == newItem.credit.id
        } else {
            false
        }
    }

    override fun areContentsTheSame(
        oldItem: BrowseMovieLoadResult.BrowseMovieLoadSuccess,
        newItem: BrowseMovieLoadResult.BrowseMovieLoadSuccess
    ): Boolean {
        return if (oldItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie &&
            newItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie
        ) {
            oldItem.movie.title == newItem.movie.title &&
                    oldItem.movie.posterPath == newItem.movie.posterPath &&
                    oldItem.movie.backdropPath == newItem.movie.backdropPath &&
                    oldItem.movie.overview == newItem.movie.overview &&
                    oldItem.movie.releaseDate == newItem.movie.releaseDate &&
                    oldItem.movie.voteAverage == newItem.movie.voteAverage &&
                    oldItem.movie.genres == newItem.movie.genres &&
                    oldItem.movie.type == newItem.movie.type &&
                    oldItem.movie.isFavorite == newItem.movie.isFavorite
        } else if (oldItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit &&
            newItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit
        ) {
            oldItem.credit.name == newItem.credit.name &&
                    oldItem.credit.profilePath == newItem.credit.profilePath
        } else {
            false
        }
    }
}