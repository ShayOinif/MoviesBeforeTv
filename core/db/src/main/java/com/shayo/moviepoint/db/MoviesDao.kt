package com.shayo.moviepoint.db

import androidx.room.*

@Dao
internal interface MoviesDao {
    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Int): DbMovie

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMovie(dbMovie: DbMovie)

    // TODO: Add clear for refreshes
}