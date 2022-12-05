package com.shayo.moviepoint.db

import androidx.paging.PagingSource

interface LocalMovieCategoryDataSource {
    fun getCategoryPaging(category: String): PagingSource<Int, MovieCategory>

    suspend fun addMovie(movieCategory: MovieCategory)

    suspend fun deleteCategory(category: String)
}

class LocalMovieCategoryDataSourceImpl(
    private val movieCategoryDao: MovieCategoryDao,
) : LocalMovieCategoryDataSource {
    override fun getCategoryPaging(category: String): PagingSource<Int, MovieCategory> {
        return movieCategoryDao.getCategoryPaging(category)
    }

    override suspend fun addMovie(movieCategory: MovieCategory) {
        movieCategoryDao.addMovie(movieCategory)
    }

    override suspend fun deleteCategory(category: String) {
        movieCategoryDao.deleteCategory(category)
    }
}