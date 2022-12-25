package com.shayo.moviespoint.getcategoriesflows

import com.shayo.movies.MovieManager
import kotlinx.coroutines.CoroutineScope

interface GetCategoriesFlowsUseCase {
    operator fun invoke(scope: CoroutineScope, withGenres: Boolean): List<Category>
}

internal class GetCategoriesFlowsUseCaseImpl(
    private val movieManager: MovieManager,
) : GetCategoriesFlowsUseCase {
    override fun invoke(scope: CoroutineScope, withGenres: Boolean): List<Category> {
        return categories.map { category ->
            Category(
                name = category,
                flow = movieManager.getCategoryFlow(
                    type = category.type,
                    category = category.category,
                    scope = scope,
                ),
            )
        }
    }

    companion object {
        private val categories = listOf(
            CategoryName.POPULAR_MOVIES,
            CategoryName.UPCOMING_MOVIES,
            CategoryName.POPULAR_SHOWS,
            CategoryName.TOP_SHOWS,
        )
    }
}