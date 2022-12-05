package com.shayo.movies

import com.shayo.network.MovieNetworkResponse

internal class MovieWithGenrePagingSource(
    loadingFun: suspend (page: Int) -> Result<MovieNetworkResponse>,
    private val genresFun: suspend () -> Result<Map<Int, Genre>>
) : MoviesPagingSource(loadingFun) {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return genresFun().fold(
            onSuccess = { genres ->
                val moviesResult = super.load(params)

                if (moviesResult is LoadResult.Page) {
                    moviesResult.copy(
                        data = moviesResult.data.map { movie ->
                            movie.copy(
                                genres = movie.genres.map { genre ->
                                    genres[genre.id] ?: run {
                                        genre
                                    }
                                }
                            )
                        }
                    )
                } else {
                    moviesResult
                }
            },
            onFailure = {
                LoadResult.Error(it)
            }
        )
    }
}