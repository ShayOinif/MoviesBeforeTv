package com.shayo.moviepoint.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GenresDao {
    @Query("SELECT * FROM genres")
    fun getGenres(): Flow<List<DbGenre>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGenres(dbGenres: List<DbGenre>)
}