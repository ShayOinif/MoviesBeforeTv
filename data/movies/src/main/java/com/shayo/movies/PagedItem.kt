package com.shayo.movies

sealed interface PagedItem {
    val position: Int

    data class PagedMovie(
        val movie: Movie,
        override val position: Int,
    ) : PagedItem

    data class PagedCredit(
        val credit: Credit,
        override val position: Int,
    ) : PagedItem
}