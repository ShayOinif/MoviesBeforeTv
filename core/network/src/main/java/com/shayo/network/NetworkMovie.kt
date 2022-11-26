package com.shayo.network

import com.google.gson.annotations.SerializedName

data class NetworkMovie(
    val id: Int,
    val title: String,
    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("backdrop_path")
    val backdropPath: String?,
    val overview: String,

    @SerializedName("release_date")
    val releaseDate: String,
)