package com.shayo.moviepoint.db

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.paging.PagingSource
import java.text.SimpleDateFormat
import java.util.*

interface LocalMovieCategoryDataSource {
    fun getCategoryPaging(type: String, category: String, position: Int): PagingSource<Int, MovieCategory>

    suspend fun addMovie(movieCategory: MovieCategory)

    suspend fun deleteCategory(type: String, category: String)

    suspend fun isUpdateNeeded(type: String, category: String): Boolean

    suspend fun reportUpdate(type: String, category: String)

    suspend fun getUniqueIds(): List<Int>
}

// TODO: Move update to different module
class LocalMovieCategoryDataSourceImpl(
    private val context: Context,
    private val movieCategoryDao: MovieCategoryDao,
) : LocalMovieCategoryDataSource {

    // TODO:
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("yyyyMMdd")

    override fun getCategoryPaging(type: String, category: String, position: Int): PagingSource<Int, MovieCategory> {
        return movieCategoryDao.getCategoryPaging(type, category, position)
    }

    override suspend fun addMovie(movieCategory: MovieCategory) {
        movieCategoryDao.addMovie(movieCategory)
    }

    override suspend fun deleteCategory(type: String, category: String) {
        movieCategoryDao.deleteCategory(type, category)
    }

    @SuppressLint("SimpleDateFormat")
    override suspend fun isUpdateNeeded(type: String, category: String): Boolean {
        return formatter.formatToInt(System.currentTimeMillis()) -
                formatter.formatToInt(
                    context.getSharedPreferences("Updates", MODE_PRIVATE)
                        .getLong("$type$category", 0)
                ) > 0 // TODO:
    }

    override suspend fun reportUpdate(type: String, category: String) {
        context.getSharedPreferences("Updates", MODE_PRIVATE).edit()
            .putLong("$type$category", System.currentTimeMillis())
            .apply()
    }

    override suspend fun getUniqueIds(): List<Int> {
        return movieCategoryDao.getUniqueIds()
    }
}

private fun SimpleDateFormat.formatToInt(time: Long) =
    format(time).toInt()