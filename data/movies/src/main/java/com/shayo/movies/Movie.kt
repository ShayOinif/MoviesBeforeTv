package com.shayo.movies

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// TODO: Maybe don't need parcelize
@Parcelize
data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val releaseDate: String?,
    val voteAverage: Double,
    val genres: List<Genre>,
    val type: String,
    val runtime: Int?,
    val popularity: Double? = null,
    val isFavorite: Boolean = false
) : Parcelable

internal fun Movie.mapGenres(genresMap: Map<Int, Genre>) =
    copy(
        genres = genres.map { genre ->

            if (genre.name.isEmpty()) {
                val name = genresMap[genre.id]?.name

                name?.let {
                    genre.copy(name = name)
                } ?: genre
            } else {
                genre
            }
        })