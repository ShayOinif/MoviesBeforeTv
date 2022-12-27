package com.shayo.moviespoint.getcategoriesflows

import androidx.paging.PagingData
import com.shayo.movies.PagedItem
import kotlinx.coroutines.flow.Flow

data class Category(
    val name: CategoryName,
    val flow: Flow<PagingData<PagedItem.PagedMovie>>
)

enum class CategoryName(
    internal val type: String,
    val category: String,
) {
    POPULAR_MOVIES("movie", "popular"),
    UPCOMING_MOVIES("movie", "upcoming"),
    POPULAR_SHOWS("tv", "popular"),
    TOP_SHOWS("tv", "top_rated"),
}