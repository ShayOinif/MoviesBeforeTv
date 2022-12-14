package com.shayo.movies

import android.annotation.SuppressLint
import androidx.paging.*
import com.shayo.moviepoint.db.*
import com.shayo.network.MovieNetworkResponse
import com.shayo.network.NetworkMovieDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

interface MoviesRepository {
    suspend fun searchLoader(query: String, page: Int): Result<MovieNetworkResponse<Int>>

    fun getCategoryFlow(
        type: String,
        category: String,
        position: Int
    ): Flow<PagingData<PagedItem.PagedMovie>>

    suspend fun getMovieById(id: Int, type: String): Result<Movie>

    fun getDetailedMovieByIdFlow(id: Int, type: String): Flow<Movie?>

    suspend fun deleteOldMovies(excludeMap: Map<Int, Void?>)

    suspend fun getCategorizedMoviesIds(): List<Int>

    suspend fun updateMovies(): Result<Void?>
}

internal class MoviesRepositoryImpl constructor(
    private val networkMovieDataSource: NetworkMovieDataSource,
    private val localMovieCategoryDataSource: LocalMovieCategoryDataSource,
    private val localMoviesDataSource: LocalMoviesDataSource,
) : MoviesRepository {

    // TODO: Get in di
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("yyyyMMdd")

    override suspend fun searchLoader(
        query: String,
        page: Int
    ): Result<MovieNetworkResponse<Int>> {
        return withContext(Dispatchers.IO) { networkMovieDataSource.searchMovies(query, page) }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getCategoryFlow(
        type: String,
        category: String,
        position: Int
    ): Flow<PagingData<PagedItem.PagedMovie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                maxSize = 200,
            ),
            pagingSourceFactory = {
                localMovieCategoryDataSource.getCategoryPaging(type, category, position)
            },
            remoteMediator = CategoryMediator(
                type,
                category,
                networkMovieDataSource::getMovies,
                localMoviesDataSource,
                localMovieCategoryDataSource,
            )
        ).flow.map {
            it.map { movieCategory ->
                with(localMoviesDataSource.getMovieById(movieCategory.id)) {
                    this?.let {
                        val genres = genreIds.split(",").let {
                            if (it.first().isEmpty()) {
                                emptyList()
                            } else {
                                it.map { Genre(it.toInt(), "") }
                            }
                        }

                        PagedItem.PagedMovie(
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
                                runtime
                            ),
                            movieCategory.position
                        )
                    } ?: throw Exception("Unknown Error") // TODO:
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    // TODO: Insert new into db
    override suspend fun getMovieById(id: Int, type: String) =
        withContext(Dispatchers.IO) {
            localMoviesDataSource.getMovieById(id)?.let {
                if (formatter.formatToInt(System.currentTimeMillis()) -
                    formatter.formatToInt(it.timeStamp) > 0
                ) {
                    getByIdNetwork(type, id)
                } else {
                    with(it) {
                        val genres = genreIds.split(",").let {
                            if (it.first().isEmpty()) {
                                emptyList()
                            } else {
                                it.map { Genre(it.toInt(), "") }
                            }
                        }

                        Result.success(
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
                                runtime
                            )
                        )
                    }
                }
            } ?: getByIdNetwork(type, id)
        }

    // TODO: Insert new into db
    override fun getDetailedMovieByIdFlow(id: Int, type: String) =
        localMoviesDataSource.getMovieByIdFlow(id).map { dbMovie ->
            dbMovie?.let {
                with(dbMovie) {
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
                        runtime
                    )
                }
            }
        }.onEach { movie ->
            if (movie == null || (movie.type == "Movie" && movie.runtime == null)) {
                getByIdNetwork(type, id)
            }
        }

    override suspend fun deleteOldMovies(excludeMap: Map<Int, Void?>) {
        localMoviesDataSource.getOldMovies(System.currentTimeMillis() - 604_800_000) // TODO: Move to const, represents a week
            .forEach {
                if (!excludeMap.containsKey(it.id))
                    localMoviesDataSource.deleteMovie(it)
            }
    }

    override suspend fun getCategorizedMoviesIds(): List<Int> {
        return localMovieCategoryDataSource.getUniqueIds()
    }

    override suspend fun updateMovies(): Result<Void?> {
        localMoviesDataSource.getAllMovies().forEach {
            val result = getByIdNetwork(it.type, it.id, it.timeStamp)

            if (result.isFailure)
                return Result.failure(result.exceptionOrNull()!!)
        }

        return Result.success(null)
    }

    private suspend fun getByIdNetwork(type: String, id: Int, oldTimeStamp: Long? = null) =
        networkMovieDataSource.getById(type, id)
            .map {
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
                            genreIds.joinToString(",") { "${it.id}" },
                            type,
                            runtime,
                            oldTimeStamp ?: System.currentTimeMillis(),
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
                        genreIds.map {
                            Genre(it.id, it.name)
                        },
                        type,
                        runtime,
                    )
                }
            }
}

private fun SimpleDateFormat.formatToInt(time: Long) =
    format(time).toInt()