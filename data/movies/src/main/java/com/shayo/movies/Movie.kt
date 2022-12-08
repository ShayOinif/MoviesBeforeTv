package com.shayo.movies

import android.os.Parcelable
import com.shayo.network.NetworkMovie
import kotlinx.parcelize.Parcelize


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
) : Parcelable

internal fun NetworkMovie.mapToMovie(type: String) =
    Movie(id, title, posterPath, backdropPath, overview, releaseDate, voteAverage, genreIds.map { Genre(it, "") }, type)