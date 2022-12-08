package com.shayo.moviepoint.db

interface LocalMoviesDataSource {
    suspend fun getMovieById(id: Int): DbMovie?

    suspend fun addMovie(dbMovie: DbMovie)
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
}