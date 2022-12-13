package com.shayo.movies

import com.shayo.network.NetworkMovie

internal fun NetworkMovie<Int>.mapToMovieWithoutGenres() =
    Movie(
        id,
        title,
        posterPath,
        backdropPath,
        overview,
        releaseDate,
        voteAverage,
        genreIds.map {
            Genre(it, "")
        },
        movieType!!,
        runtime
    )