package com.shayo.network

import com.google.gson.annotations.SerializedName

data class NetworkMovie<T>(
    val id: Int,
    @SerializedName("title", alternate = ["name"])
    val title: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    val overview: String,
    @SerializedName("release_date", alternate = ["first_air_date"])
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("genre_ids", alternate = ["genres"])
    val genreIds: List<T>,
    @SerializedName("original_language")
    val language: String,
    @SerializedName("media_type")
    val movieType: String? = null,
    val runtime: Int? = null,
)