package com.shayo.moviepoint.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QueryDao {
    @Query("SELECT * FROM queries ORDER BY time DESC")
    fun getQueries(): Flow<List<DbQuery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(dbQuery: DbQuery)

    @Query("DELETE FROM queries")
    suspend fun deleteAll()
}