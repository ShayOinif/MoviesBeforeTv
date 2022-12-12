package com.shayo.moviepoint.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MoviesDao {
    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Int): DbMovie?

    @Query("SELECT * FROM movies WHERE id = :id")
    fun getMovieByIdFlow(id: Int): Flow<DbMovie?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMovie(dbMovie: DbMovie)

    @Delete
    suspend fun deleteMovie(dbMovie: DbMovie)

    @Query("SELECT * FROM movies WHERE timeStamp <= :timeStamp")
    suspend fun getOldMovies(timeStamp: Long): List<DbMovie>

    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<DbMovie>
}