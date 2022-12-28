package com.shayo.movies

import android.annotation.SuppressLint
import androidx.paging.*
import com.shayo.moviepoint.db.*
import com.shayo.network.MovieNetworkResponse
import com.shayo.network.NetworkMovieDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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

    suspend fun discover(
        type: String,
    ): Result<List<Movie>>

    suspend fun updateCategory(
        type: String,
        category: String,
    ): Boolean
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
    override suspend fun updateCategory(
        type: String,
        category: String,
    ): Boolean {
        return CategoryMediator(
            type,
            category,
            networkMovieDataSource::getMovies,
            localMoviesDataSource,
            localMovieCategoryDataSource,
        ).run {
            if (initialize() == RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH) {
                load(
                    LoadType.REFRESH,
                    PagingState(emptyList(), null, PagingConfig(pageSize = 20), 0)
                ) is RemoteMediator.MediatorResult.Success
            } else {
                true
            }
        }
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

    override suspend fun discover(type: String): Result<List<Movie>> {
        return networkMovieDataSource.discover(type).map { response ->
            response.results.map {
                it.mapToMovieWithoutGenres(type)
            }
        }
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