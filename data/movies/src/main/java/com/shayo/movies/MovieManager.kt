package com.shayo.movies

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MovieManager {
    fun getMoviesWithGenrePager(category: String): Pager<Int, Movie>

    fun getSearchWithGenrePager(query: String): Pager<Int, Movie>

    val favoritesMap: Flow<Map<Int, Int>>

    fun getFavoritesPager(): Flow<PagingData<Movie>>
}

internal class MovieManagerImpl(
    private val moviesRepository: MoviesRepository,
    private val genreRepository: GenreRepository,
) : MovieManager {
    override fun getMoviesWithGenrePager(category: String): Pager<Int, Movie> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = true,
                initialLoadSize = 20,
                maxSize = 200,
                jumpThreshold = 60,
            ),
            pagingSourceFactory = {
                MovieWithGenrePagingSource(
                    // TODO:
                    {
                        moviesRepository.loader(category, it)
                    },
                    {
                        genreRepository.getMoviesGenres()
                    }
                )
            }
        )
    }

    override fun getSearchWithGenrePager(query: String): Pager<Int, Movie> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = true,
                initialLoadSize = 20,
                maxSize = 200,
                jumpThreshold = 60,
            ),
            pagingSourceFactory = {
                MovieWithGenrePagingSource(
                    // TODO:
                    {
                        moviesRepository.searchLoader(query, it)
                    },
                    {
                        genreRepository.getMoviesGenres()
                    }
                )
            }
        )
    }

    override val favoritesMap = moviesRepository.favoritesMap

    override fun getFavoritesPager(): Flow<PagingData<Movie>> {
        return moviesRepository.getFavoritesPager()
            .map {
                it.map {
                    val genres = genreRepository.getMoviesGenres()

                    if (genres.isSuccess) {
                        it.copy(
                            genres = it.genres.map { genre ->
                                // TODO: Solve in a simpler way

                                val name = genres.getOrNull()?.get(genre.id)?.name

                                name?.let {
                                    genre.copy(name = name)
                                } ?: genre
                            })
                    } else
                        it
                }
            }
    }
}