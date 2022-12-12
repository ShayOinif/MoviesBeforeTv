package com.shayo.moviepoint.db

import kotlinx.coroutines.flow.Flow

interface LocalMoviesDataSource {
    suspend fun getMovieById(id: Int): DbMovie?

    suspend fun addMovie(dbMovie: DbMovie)

    fun getMovieByIdFlow(id: Int): Flow<DbMovie?>

    suspend fun deleteMovie(dbMovie: DbMovie)

    suspend fun getOldMovies(timeStamp: Long): List<DbMovie>

    suspend fun getAllMovies(): List<DbMovie>
}

internal class LocalMoviesDataSourceImpl(
    private val moviesDao: MoviesDao,
) : LocalMoviesDataSource {

    override suspend fun getMovieById(id: Int): DbMovie? {
        return moviesDao.getMovieById(id)
    }

    override suspend fun addMovie(dbMovie: DbMovie) {
        moviesDao.addMovie(dbMovie)
    }

    override fun getMovieByIdFlow(id: Int): Flow<DbMovie?> {
        return moviesDao.getMovieByIdFlow(id)
    }

    // TODO: Return result, not only here!
    override suspend fun deleteMovie(dbMovie: DbMovie) {
        moviesDao.deleteMovie(dbMovie)
    }

    override suspend fun getOldMovies(timeStamp: Long): List<DbMovie> {
        return moviesDao.getOldMovies(timeStamp)
    }

    override suspend fun getAllMovies(): List<DbMovie> {
        return moviesDao.getAllMovies()
    }
}