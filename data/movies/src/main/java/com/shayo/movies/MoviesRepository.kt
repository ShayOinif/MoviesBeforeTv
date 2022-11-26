package com.shayo.movies

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.shayo.network.NetworkMovieDataSource

interface MoviesRepository {
    suspend fun getMovies(category: String): Result<List<Movie>>

    fun getMoviesPager(category: String): Pager<Int, Movie>
}

internal class MoviesRepositoryImpl constructor(
    private val networkMovieDataSource: NetworkMovieDataSource,
) : MoviesRepository {
    override suspend fun getMovies(category: String): Result<List<Movie>> {
        return networkMovieDataSource.getMovies(category)
            .map { response ->
                response.results.map { networkMovie ->
                    networkMovie.mapToMovie()
                }
            }
    }

    override fun getMoviesPager(category: String): Pager<Int, Movie> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = true,
                initialLoadSize = 20,
                maxSize = 200,
                jumpThreshold = 60,
            ),
            pagingSourceFactory = {
                MoviesPagingSource { page ->
                    networkMovieDataSource.getMovies(category, page)
                }
            }
        )
    }
}