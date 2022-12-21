package com.shayo.movies

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shayo.network.MovieNetworkResponse

internal open class SearchPagingSource(
    private val afterPosition: Int,
    private val loadingFun: suspend (page: Int) -> Result<MovieNetworkResponse<Int>>,
) : PagingSource<Int, PagedItem>() {

    override val jumpingSupported = true

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, PagedItem> {
        // Start refresh at page 1 if undefined.
        val nextPageNumber = params.key
            ?: 1 // TODO: Check against afterPosition and consider ramifications in larger scales

        val result = loadingFun(nextPageNumber)

        return result.fold(
            onSuccess = { response ->

                var data = response.results
                    .mapIndexed { index, networkMovie ->
                        val position = index + (nextPageNumber - 1) * 20

                        with(networkMovie) {
                            if (movieType == "person") {
                                PagedItem.PagedCredit(
                                    credit = Credit(
                                        id = id,
                                        name = title,
                                        profilePath = posterPath,
                                        knownFor = knownFor.map { it.mapToMovieWithoutGenres() }
                                    ),
                                    position = position,
                                )
                            } else {
                                PagedItem.PagedMovie(
                                    mapToMovieWithoutGenres(),
                                    position = position,
                                )
                            }
                        }
                    }

                if (afterPosition / 20 == nextPageNumber - 1) {
                    data = data.drop(afterPosition % 20)
                }

                LoadResult.Page(
                    data = data,
                    prevKey = if (nextPageNumber == 1 || (afterPosition / 20 == nextPageNumber - 1)) null else nextPageNumber - 1,
                    nextKey = if (nextPageNumber == response.totalPages) null else nextPageNumber + 1,
                )
            },
            onFailure = {
                LoadResult.Error(it)
            }
        )
    }

    // TODO: Observe what happens with the new afterPosition property
    override fun getRefreshKey(state: PagingState<Int, PagedItem>): Int? {
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
