package com.shayo.movies

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.shayo.moviepoint.db.DbMovie
import com.shayo.moviepoint.db.LocalFavoritesDataSource
import com.shayo.network.MovieNetworkResponse
import com.shayo.network.NetworkMovieDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MoviesRepository {
    suspend fun getMovies(category: String): Result<List<Movie>>

    fun getMoviesPager(category: String): Pager<Int, Movie>

    suspend fun loader(category: String, page: Int): Result<MovieNetworkResponse>

    fun getSearchPager(query: String): Pager<Int, Movie>

    suspend fun searchLoader(query: String, page: Int): Result<MovieNetworkResponse>

    suspend fun toggleFavorite(movie: Movie)

    val favoritesMap: Flow<Map<Int, Int>>

    fun getFavoritesPager(): Flow<PagingData<Movie>>
}

internal class MoviesRepositoryImpl constructor(
    private val networkMovieDataSource: NetworkMovieDataSource,
    private val localFavoritesDataSource: LocalFavoritesDataSource,
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

    override suspend fun loader(category: String, page: Int) =
        networkMovieDataSource.getMovies(category, page)

    override fun getSearchPager(query: String): Pager<Int, Movie> {
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
                    networkMovieDataSource.searchMovies(query, page)
                }
            }
        )
    }

    override suspend fun searchLoader(query: String, page: Int): Result<MovieNetworkResponse> {
        return networkMovieDataSource.searchMovies(query, page)
    }

    override suspend fun toggleFavorite(movie: Movie) {
        localFavoritesDataSource.toggleFavorite(
            with(movie) {
                DbMovie(
                    id, title, posterPath, backdropPath,
                    overview,
                    releaseDate,
                    voteAverage,
                    genres.joinToString(separator = ",") {
                        it.id.toString()
                    },
                )
            }
        )
    }

    override val favoritesMap = localFavoritesDataSource.favoritesMapFlow

    // TODO: return flow in other pagers
    override fun getFavoritesPager(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = true,
                initialLoadSize = 20,
                maxSize = 200,
                jumpThreshold = 60,
            ),
            pagingSourceFactory = {
                localFavoritesDataSource.getFavoritesPaging()
            }
        ).flow.map {
            it.map {
                with (it) {
                    Movie(
                        id, title, posterPath, backdropPath, overview, releaseDate, voteAverage, genreIds.split(",").map { Genre(it.toInt(), "") }
                    )
                }
            }
        }
    }
}