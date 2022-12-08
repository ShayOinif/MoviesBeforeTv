package com.shayo.movies

import androidx.paging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface MovieManager {
    fun getSearchFlow(query: String, scope: CoroutineScope): Flow<PagingData<Movie>>

    val favoritesMap: Flow<Map<Int, String>>

    fun getFavoritesFlow(): Flow<List<Movie>>

    fun setCollection(collectionName: String?)

    suspend fun toggleFavorite(movie: Movie)

    fun getCategoryFlow(type: String, category: String, scope: CoroutineScope): Flow<PagingData<Movie>>
}

internal class MovieManagerImpl(
    private val moviesRepository: MoviesRepository,
    private val genreRepository: GenreRepository,
    private val favoritesRepository: FavoritesRepository,
) : MovieManager {

    override fun getSearchFlow(query: String, scope: CoroutineScope): Flow<PagingData<Movie>> {
        return combine(
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    initialLoadSize = 20,
                    maxSize = 200,
                )
            ) {
                MoviesPagingSource {
                    moviesRepository.searchLoader(query, it)
                }
            }.flow.cachedIn(scope),
            genreRepository.movieGenresFlow
        ) { moviePagingData, genres ->
            moviePagingData.map {
                it.mapGenres(genres)
            }
        }.cachedIn(scope)
    }

    override val favoritesMap = favoritesRepository.favoritesMap

    override fun getFavoritesFlow(): Flow<List<Movie>> {
        return combine(
            favoritesRepository.favoritesMap,
            genreRepository.movieGenresFlow
        ) { favoritesMap, genres ->
            favoritesMap.map { (id, type) ->
                moviesRepository.getMovieById(id, type)!!
                    .mapGenres(genres)
            }
        }
    }

    override fun setCollection(collectionName: String?) {
        favoritesRepository.setCollection(collectionName)
    }

    override suspend fun toggleFavorite(movie: Movie) {
        favoritesRepository.toggleFavorite(movie.id, movie.type)
    }

    override fun getCategoryFlow(type: String, category: String, scope: CoroutineScope): Flow<PagingData<Movie>> {
        return combine(
            moviesRepository.getCategoryFlow(type, category).cachedIn(scope),
            genreRepository.movieGenresFlow
        ) { moviePagingData, genres ->
            moviePagingData.map {
                it.mapGenres(genres)
            }
        }.cachedIn(scope)
    }

    private fun Movie.mapGenres(genresMap: Map<Int, Genre>) =
        copy(
            genres = genres.map { genre ->

                if (genre.name.isEmpty()) {
                    val name = genresMap[genre.id]?.name

                    name?.let {
                        genre.copy(name = name)
                    } ?: genre
                } else {
                    genre
                }
            })
}