package com.shayo.movies

data class Credit(
    val id: Int,
    val name: String,
    val profilePath: String?,
    val description: String = "",
    val knownFor: List<Movie> = emptyList()
)