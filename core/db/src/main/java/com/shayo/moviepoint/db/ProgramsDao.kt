package com.shayo.moviepoint.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProgramsDao {
    @Query("SELECT * FROM programs")
    suspend fun getAll(): List<DbProgram>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(dbProgram: DbProgram)

    @Query("DELETE FROM programs")
    suspend fun deleteAll()
}