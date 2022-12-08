package com.shayo.moviepoint.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// TODO: Make a relation and embedded for easier access
@Dao
interface MovieCategoryDao {
    @Query("SELECT * FROM movie_category WHERE category = :category AND type = :type ORDER BY position ASC")
    fun getCategoryPaging(type: String, category: String): PagingSource<Int, MovieCategory>

    @Query("SELECT COUNT(*) FROM movie_category WHERE category = :category AND type = :type")
    suspend fun isCategoryEmpty(type: String, category: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMovie(movieCategory: MovieCategory)

    @Query("DELETE FROM movie_category WHERE category = :category AND type = :type")
    suspend fun deleteCategory(type: String, category: String)
}