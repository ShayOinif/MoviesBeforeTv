package com.shayo.moviepoint.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// TODO: Make a relation and embedded for easier access
@Dao
interface MovieCategoryDao {
    @Query("SELECT * FROM movie_category WHERE category = :category ORDER BY position ASC")
    fun getCategoryPaging(category: String): PagingSource<Int, MovieCategory>

    @Insert
    suspend fun addMovie(movieCategory: MovieCategory)

    @Query("DELETE FROM movie_category WHERE category = :category")
    suspend fun deleteCategory(category: String)
}