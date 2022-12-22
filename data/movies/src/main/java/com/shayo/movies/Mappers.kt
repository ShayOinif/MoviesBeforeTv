package com.shayo.movies

import com.shayo.network.NetworkMovie

fun NetworkMovie<Int>.mapToMovieWithoutGenres(type: String? = null) =
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
        type ?: movieType ?: throw(Exception("Error in data:movies Mappers.kt/mapToMovieWithoutGenres, couldn't get type")),
        runtime,
        popularity
    )