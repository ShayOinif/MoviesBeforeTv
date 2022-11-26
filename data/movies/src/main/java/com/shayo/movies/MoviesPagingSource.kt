package com.shayo.movies

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shayo.network.MovieNetworkResponse

internal class MoviesPagingSource(
    private val loadingFun: suspend (page: Int) -> Result<MovieNetworkResponse>,
) : PagingSource<Int, Movie>() {

    override val jumpingSupported = true

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Movie> {
        // Start refresh at page 1 if undefined.
        val nextPageNumber = params.key ?: 1

        val result = loadingFun(nextPageNumber)

        return result.fold(
            onSuccess = { response ->
                val before = (nextPageNumber - 1) * 20
                val after = response.totalResults - before + 20

                LoadResult.Page(
                    data = response.results.map { networkMovie ->
                        networkMovie.mapToMovie()
                    },
                    prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,
                    nextKey = if (nextPageNumber == response.totalPages) null else nextPageNumber + 1,
                    itemsBefore = before,
                    itemsAfter = after,
                    )
            },
            onFailure = {
                LoadResult.Error(it)
            }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
