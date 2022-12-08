package com.shayo.movies

import android.annotation.SuppressLint
import androidx.paging.*
import com.shayo.moviepoint.db.*
import com.shayo.moviespoint.firestore.FirestoreFavoritesDataSourceImpl
import com.shayo.network.MovieNetworkResponse
import com.shayo.network.NetworkMovieDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat

interface MoviesRepository {
    suspend fun searchLoader(query: String, page: Int): Result<MovieNetworkResponse>

    fun getCategoryFlow(type: String, category: String): Flow<PagingData<Movie>>

    suspend fun getMovieById(id: Int, type: String): Movie?
}

internal class MoviesRepositoryImpl constructor(
    private val networkMovieDataSource: NetworkMovieDataSource,
    private val localMovieCategoryDataSource: LocalMovieCategoryDataSource,
    private val localMoviesDataSource: LocalMoviesDataSource,
) : MoviesRepository {

    // TODO: Get in di
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("yyyyMMdd")


    // TODO: Get in DI
    private val mixedFavoritesDataSource = FirestoreFavoritesDataSourceImpl()

    override suspend fun searchLoader(query: String, page: Int): Result<MovieNetworkResponse> {
        return networkMovieDataSource.searchMovies(query, page)
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getCategoryFlow(type: String, category: String): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                maxSize = 200,
            ),
            pagingSourceFactory = {
                localMovieCategoryDataSource.getCategoryPaging(type, category)
            },
            remoteMediator = CategoryMediator(
                type,
                category,
                networkMovieDataSource::getMovies,
                localMoviesDataSource,
                localMovieCategoryDataSource,
            )
        ).flow.map {
            it.map {
                with(localMoviesDataSource.getMovieById(it.id)) {
                    this?.let {
                        val genres = genreIds.split(",").let {
                            if (it.first().isEmpty()) {
                                emptyList<Genre>()
                            } else {
                                it.map { Genre(it.toInt(), "") }
                            }
                        }

                        Movie(
                            id,
                            title,
                            posterPath,
                            backdropPath,
                            overview,
                            releaseDate,
                            voteAverage,
                            genres,
                            type,
                        )
                    } ?: throw Exception("Unknown Error") // TODO:
                }
            }
        }
    }

    override suspend fun getMovieById(id: Int, type: String) =
        localMoviesDataSource.getMovieById(id)?.let {
            if (formatter.formatToInt(System.currentTimeMillis()) -
                formatter.formatToInt(it.timeStamp) > 0
            ) // TODO:)
            {
                networkMovieDataSource.getById(type, id)
                    .fold(
                        onSuccess = {
                            with(it) {

                                localMoviesDataSource.addMovie(
                                    DbMovie(
                                        id,
                                        title,
                                        posterPath,
                                        backdropPath,
                                        overview,
                                        releaseDate,
                                        voteAverage,
                                        genres.joinToString(",") { "${it.id}" },
                                        type,
                                        java.lang.System.currentTimeMillis()
                                    )
                                )

                                Movie(
                                    id,
                                    title,
                                    posterPath,
                                    backdropPath,
                                    overview,
                                    releaseDate,
                                    voteAverage,
                                    genres.map {
                                        Genre(it.id, it.name)
                                    },
                                    type,
                                )
                            }
                        },
                        onFailure = {
                            throw it // TODO:
                        }
                    )
            } else {
                with(it) {
                    val genres = genreIds.split(",").let {
                        if (it.first().isEmpty()) {
                            emptyList()
                        } else {
                            it.map { Genre(it.toInt(), "") }
                        }
                    }

                    Movie(
                        id,
                        title,
                        posterPath,
                        backdropPath,
                        overview,
                        releaseDate,
                        voteAverage,
                        genres,
                        type,
                    )
                }
            }
        } ?: networkMovieDataSource.getById(type, id)
            .fold(
                onSuccess = {
                    with(it) {

                        localMoviesDataSource.addMovie(
                            DbMovie(
                                id,
                                title,
                                posterPath,
                                backdropPath,
                                overview,
                                releaseDate,
                                voteAverage,
                                genres.joinToString(",") { "${it.id}" },
                                type,
                                java.lang.System.currentTimeMillis()
                            )
                        )

                        Movie(
                            id,
                            title,
                            posterPath,
                            backdropPath,
                            overview,
                            releaseDate,
                            voteAverage,
                            genres.map {
                                Genre(it.id, it.name)
                            },
                            type,
                        )
                    }
                },
                onFailure = {
                    throw it // TODO:
                }
            )
}

private fun SimpleDateFormat.formatToInt(time: Long) =
    format(time).toInt()