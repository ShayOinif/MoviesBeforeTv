package com.shayo.moviespoint.ui

data class MediaCardItem(
    val posterPath: String?,
    val title: String,
    val voteAverage: String,
    val releaseDate: String?,
    val type: String,
    val inWatchlist: Boolean? = false,
)