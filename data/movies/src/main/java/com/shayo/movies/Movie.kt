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
    val releaseDate: String,
) : Parcelable

internal fun NetworkMovie.mapToMovie() =
    Movie(id, title, posterPath, backdropPath, overview, releaseDate)