package com.shayo.movies

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.shayo.moviepoint.db.DbMovie
import com.shayo.moviepoint.db.LocalMovieCategoryDataSource
import com.shayo.moviepoint.db.LocalMoviesDataSource
import com.shayo.moviepoint.db.MovieCategory
import com.shayo.network.MovieNetworkResponse

@OptIn(ExperimentalPagingApi::class)
class CategoryMediator(
    private val type: String,
    private val category: String,
    private val network: suspend (type: String, category: String, page: Int) -> Result<MovieNetworkResponse>,
    private val localMoviesDataSource: LocalMoviesDataSource,
    private val localMovieCategoryDataSource: LocalMovieCategoryDataSource,
) : RemoteMediator<Int, MovieCategory>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieCategory>
    ): MediatorResult {
        val loadKey = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND ->
                return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )

                lastItem.position / 20 + 2
            }
        }

        Log.d("MyTag1","Trying to get page $loadKey")

        return network(type, category, loadKey).fold(
            onSuccess = { response ->

                Log.d("MyTag1","got page $loadKey")

                //database.withTransaction { TODO:
                if (loadType == LoadType.REFRESH) {
                    localMovieCategoryDataSource.deleteCategory(type, category)
                }

                response.results.forEachIndexed { index, networkMovie ->

                    if (networkMovie.language == "en") {
                        with(networkMovie) {
                            localMoviesDataSource.addMovie(
                                DbMovie(
                                    id,
                                    title,
                                    posterPath,
                                    backdropPath,
                                    overview,
                                    releaseDate,
                                    voteAverage,
                                    genreIds.joinToString(separator = ",") { it.toString() },
                                    type,
                                    System.currentTimeMillis()
                                )
                            )

                            localMovieCategoryDataSource.addMovie(
                                MovieCategory(
                                    id, category, type, ((loadKey - 1) * 20) + index
                                )
                            )
                        }
                    }
                }

                localMovieCategoryDataSource.reportUpdate(type, category)

                MediatorResult.Success(
                    endOfPaginationReached = response.page == response.totalPages
                )
            },
            onFailure = {
                Log.d("MyTag1","Error get page $loadKey")
                MediatorResult.Error(it)
            }
        )
    }

    override suspend fun initialize(): InitializeAction {
        return if (localMovieCategoryDataSource.isUpdateNeeded(type, category))
        {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }
}