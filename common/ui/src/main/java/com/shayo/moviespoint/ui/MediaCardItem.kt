package com.shayo.moviespoint.ui

data class MediaCardItem(
    val id: Int,
    val posterPath: String?,
    val title: String,
    val voteAverage: String,
    val releaseDate: String?,
    val inWatchlist: Boolean = false,
)