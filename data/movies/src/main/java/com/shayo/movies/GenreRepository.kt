package com.shayo.movies

import com.shayo.network.NetworkGenreDataSource

// TODO: Move to another module
interface GenreRepository {
    suspend fun getMoviesGenres(): Result<Map<Int, Genre>>
}

internal class GenreRepositoryImpl constructor(
    private val networkGenreDataSource: NetworkGenreDataSource,
) : GenreRepository {
    override suspend fun getMoviesGenres(): Result<Map<Int, Genre>> {

        return if (genres != null && genres!!.isSuccess) {
            genres!!
        } else {
            networkGenreDataSource.getMoviesGenres()
                .map { networkGenres ->
                    networkGenres.map { networkGenre ->
                        Genre(
                            networkGenre.id,
                            networkGenre.name
                        )
                    }.associateBy {
                        it.id
                    }
                }.also { newResult ->
                    genres = newResult
                }
        }
    }

    companion object {
        // TODO:
        private var genres: Result<Map<Int, Genre>>? = null
    }
}