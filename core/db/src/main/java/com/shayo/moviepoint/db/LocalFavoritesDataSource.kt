package com.shayo.moviepoint.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LocalFavoritesDataSource {
    suspend fun toggleFavorite(dbFavorite: DbFavorite)

    val favoritesMapFlow: Flow<Map<Int, DbFavorite>>
}

internal class LocalFavoritesDataSourceImpl(
    private val favoritesDao: FavoritesDao,
) : LocalFavoritesDataSource {
    override suspend fun toggleFavorite(dbFavorite: DbFavorite) {
        favoritesDao.getFavoriteById(dbFavorite.id)?.let {
            favoritesDao.removeFavorite(dbFavorite)
        } ?: favoritesDao.addFavorite(dbFavorite)
    }

    override val favoritesMapFlow =
        favoritesDao.favoritesMapFlow()
            .map {
                it.associateBy { it.id }
            }
}