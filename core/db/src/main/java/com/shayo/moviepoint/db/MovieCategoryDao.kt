package com.shayo.moviepoint.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// TODO: Make a relation and embedded for easier access
@Dao
interface MovieCategoryDao {
    // TODO: Find out why it causes problems in detail screen when scrolling beyond
    @Query("SELECT * FROM movie_category WHERE category = :category AND type = :type AND position >= :position ORDER BY position ASC")
    fun getCategoryPaging(
        type: String,
        category: String,
        position: Int
    ): PagingSource<Int, MovieCategory>

    @Query("SELECT COUNT(*) FROM movie_category WHERE category = :category AND type = :type")
    suspend fun isCategoryEmpty(type: String, category: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMovie(movieCategory: MovieCategory)

    @Query("DELETE FROM movie_category WHERE category = :category AND type = :type")
    suspend fun deleteCategory(type: String, category: String)

    @Query("SELECT DISTINCT id FROM movie_category")
    suspend fun getUniqueIds(): List<Int>
}