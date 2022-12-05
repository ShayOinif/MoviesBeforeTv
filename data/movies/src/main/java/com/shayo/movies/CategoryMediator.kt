package com.shayo.movies

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.shayo.moviepoint.db.LocalMovieCategoryDataSource
import com.shayo.moviepoint.db.LocalMoviesDataSource
import com.shayo.network.MovieNetworkResponse

@OptIn(ExperimentalPagingApi::class)
class CategoryMediator(
    private val category: String,
    private val network: suspend (category: String, page: Int) -> Result<MovieNetworkResponse>,
    private val localMoviesDataSource: LocalMoviesDataSource,
    private val localMovieCategoryDataSource: LocalMovieCategoryDataSource,
): RemoteMediator<Int, Movie>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Movie>
    ): RemoteMediator.MediatorResult {

        
TODO()
    }
}