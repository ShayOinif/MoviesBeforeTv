package com.shayo.movies

import androidx.paging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// TODO: Break into use cases and move to domain module
interface MovieManager {
    fun getSearchFlow(
        query: String,
        scope: CoroutineScope,
        position: Int = 0,
        withGenres: Boolean = true,
    ): Flow<PagingData<PagedItem>>

    // TODO: Handle differently, especially for reloading once network available again
    fun getFavoritesFlow(
        withGenres: Boolean = true,
    ): Flow<List<Result<Movie>>>

    suspend fun toggleFavorite(id: Int, type: String)

    fun getCategoryFlow(
        type: String,
        category: String,
        scope: CoroutineScope,
        position: Int = 0,
        withGenres: Boolean = true,
    ): Flow<PagingData<PagedItem.PagedMovie>>

    fun getDetailedMovieByIdFlow(
        id: Int,
        type: String,
        withGenres: Boolean = true,
    ): Flow<Movie?>

    val favoritesMap: Flow<Map<Int, String>>
}

internal class MovieManagerImpl(
    private val moviesRepository: MoviesRepository,
    private val genreRepository: GenreRepository,
    private val favoritesRepository: FavoritesRepository,
    private val userRepository: UserRepository,
) : MovieManager {

    private val coroutineScope = CoroutineScope((SupervisorJob()))

    init {
        coroutineScope.launch {
            userRepository.currentAuthUserFlow.collectLatest { currentUser ->
                favoritesRepository.setCollection(currentUser?.email)
            }
        }
    }

    override fun getSearchFlow(
        query: String,
        scope: CoroutineScope,
        position: Int,
        withGenres: Boolean,
    ): Flow<PagingData<PagedItem>> {
        return if (withGenres) {
            combine(
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        initialLoadSize = 20,
                        maxSize = 200,
                    ),
                    initialKey = (position / 20) + 1
                ) {
                    SearchPagingSource(position) {
                        moviesRepository.searchLoader(query, it)
                    }
                }.flow.cachedIn(scope),
                genreRepository.movieGenresFlow,
                favoritesRepository.favoritesMap,
            ) { moviePagingData, genres, favoritesMap ->
                moviePagingData.map {
                    if (it is PagedItem.PagedMovie) {
                        it.copy(
                            movie = it.movie.mapGenres(genres)
                                .copy(isFavorite = favoritesMap.containsKey(it.movie.id))
                        )
                    } else it
                }
            }
        } else {
            combine(
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        initialLoadSize = 20,
                        maxSize = 200,
                    ),
                    initialKey = (position / 20) + 1
                ) {
                    SearchPagingSource(position) {
                        moviesRepository.searchLoader(query, it)
                    }
                }.flow.cachedIn(scope),
                favoritesRepository.favoritesMap,
            ) { moviePagingData, favoritesMap ->
                moviePagingData.map {
                    if (it is PagedItem.PagedMovie) {
                        it.copy(movie = it.movie.copy(isFavorite = favoritesMap.containsKey(it.movie.id)))
                    } else it
                }
            }
        }.cachedIn(scope)
    }

    override fun getFavoritesFlow(
        withGenres: Boolean,
    ) = if (withGenres) {
        combine(
            favoritesRepository.favoritesMap,
            genreRepository.movieGenresFlow
        ) { favoritesMap, genres ->
            favoritesMap.map { (id, type) ->
                moviesRepository.getMovieById(id, type).map { movie ->
                    movie.mapGenres(genres).copy(isFavorite = true)
                }
            }
        }
    } else {
        favoritesRepository.favoritesMap.map { favoritesMap ->
            favoritesMap.map { (id, type) ->
                moviesRepository.getMovieById(id, type).map { movie ->
                    movie.copy(isFavorite = true)
                }
            }
        }
    }

    override suspend fun toggleFavorite(id: Int, type: String) {
        favoritesRepository.toggleFavorite(id, type)
    }

    override fun getCategoryFlow(
        type: String,
        category: String,
        scope: CoroutineScope,
        position: Int,
        withGenres: Boolean,
    ): Flow<PagingData<PagedItem.PagedMovie>> {
        return if (withGenres) {
            combine(
                moviesRepository.getCategoryFlow(type, category, position).cachedIn(scope),
                genreRepository.movieGenresFlow,
                favoritesRepository.favoritesMap,
            ) { moviePagingData, genres, favoritesMap ->
                moviePagingData.map {
                    it.copy(
                        movie = it.movie.mapGenres(genres)
                            .copy(isFavorite = favoritesMap.containsKey(it.movie.id))
                    )
                }
            }
        } else {
            combine(
                moviesRepository.getCategoryFlow(type, category, position).cachedIn(scope),
                favoritesRepository.favoritesMap,
            ) { moviePagingData, favoritesMap ->
                moviePagingData.map {
                    it.copy(movie = it.movie.copy(isFavorite = favoritesMap.containsKey(it.movie.id)))
                }
            }
        }.cachedIn(scope)
    }

    override fun getDetailedMovieByIdFlow(
        id: Int,
        type: String,
        withGenres: Boolean,
    ): Flow<Movie?> {
        return if (withGenres) {
            combine(
                moviesRepository.getDetailedMovieByIdFlow(id, type),
                favoritesRepository.favoritesMap,
                genreRepository.movieGenresFlow,
            ) { movie, favoriteMap, genres ->
                movie?.let {
                    it.copy(isFavorite = favoriteMap.containsKey(it.id)).mapGenres(genres)
                }
            }
        } else {
            combine(
                moviesRepository.getDetailedMovieByIdFlow(id, type),
                favoritesRepository.favoritesMap,
            ) { movie, favoriteMap ->
                movie?.let {
                    it.copy(isFavorite = favoriteMap.containsKey(it.id))
                }
            }
        }
    }

    override val favoritesMap = favoritesRepository.favoritesMap
}