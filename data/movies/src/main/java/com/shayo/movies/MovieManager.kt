package com.shayo.movies

import androidx.paging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn

interface MovieManager {
    fun getSearchFlow(query: String, scope: CoroutineScope): Flow<PagingData<Movie>>

    val favoritesMap: Flow<Map<Int, String>>

    // TODO: Handle differently, especially for reloading once network available again
    val favoritesFlow: Flow<List<Result<Movie>>>

    fun setCollection(collectionName: String?)

    suspend fun toggleFavorite(movie: Movie)

    fun getCategoryFlow(type: String, category: String, scope: CoroutineScope, position: Int = 0): Flow<PagingData<PagedMovie>>

    fun getDetailedMovieByIdFlow(id: Int, type: String): Flow<Movie?>
}

internal class MovieManagerImpl(
    private val moviesRepository: MoviesRepository,
    private val genreRepository: GenreRepository,
    private val favoritesRepository: FavoritesRepository,
) : MovieManager {

    private val coroutineScope = CoroutineScope((SupervisorJob()))

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

    override val favoritesFlow = combine(
            favoritesRepository.favoritesMap,
            genreRepository.movieGenresFlow
        ) { favoritesMap, genres ->
            favoritesMap.map { (id, type) ->
                moviesRepository.getMovieById(id, type).map { movie ->
                    movie.mapGenres(genres)
                }
            }
        }.shareIn(coroutineScope, SharingStarted.WhileSubscribed(1_500), replay = 1)

    override fun setCollection(collectionName: String?) {
        favoritesRepository.setCollection(collectionName)
    }

    override suspend fun toggleFavorite(movie: Movie) {
        favoritesRepository.toggleFavorite(movie.id, movie.type)
    }

    override fun getCategoryFlow(type: String, category: String, scope: CoroutineScope, position: Int): Flow<PagingData<PagedMovie>> {
        return combine(
            moviesRepository.getCategoryFlow(type, category, position).cachedIn(scope),
            genreRepository.movieGenresFlow
        ) { moviePagingData, genres ->
            moviePagingData.map {
                it.copy(movie = it.movie.mapGenres(genres))
            }
        }.cachedIn(scope)
    }

    override fun getDetailedMovieByIdFlow(id: Int, type: String): Flow<Movie?> {
        return combine(
            moviesRepository.getDetailedMovieByIdFlow(id, type),
            favoritesMap,
            genreRepository.movieGenresFlow,
        ) { movie, favoriteMap, genres ->
            movie?.let {
                it.copy(isFavorite = favoriteMap.containsKey(it.id)).mapGenres(genres)
            }
        }
    }
}