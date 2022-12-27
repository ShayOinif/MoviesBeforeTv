package com.shayo.moviespoint.person

import com.shayo.movies.Movie

data class Person(
    val id: Int,
    val biography: String,
    val name: String,
    val profilePath: String?,
    val combinedCredits: CombinedCredits,
    val knownForDepartment: String?,
)

data class CombinedCredits(
    val cast: List<Movie>,
    val crew: List<Movie>,
)