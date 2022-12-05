package com.shayo.moviepoint.db

import androidx.paging.PagingSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

interface LocalFavoritesDataSource {
    fun getFavoritesPaging(): PagingSource<Int, DbFavorite>

    suspend fun toggleFavorite(dbFavorite: DbFavorite) // TODO: Handle result

    val favoritesMapFlow: Flow<Map<Int, Int>>
}

internal class LocalFavoritesDataSourceImpl(
    private val favoritesDao: FavoritesDao,
    scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) : LocalFavoritesDataSource {


    override fun getFavoritesPaging(): PagingSource<Int, DbFavorite> {
        return favoritesDao.getFavoritesPaging()
    }

    override val favoritesMapFlow = favoritesDao.getFavoritesMap()
        .map {
            it.associateBy {
                it
            }
        }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), // TODO:
            replay = 1,
        )

    override suspend fun toggleFavorite(dbFavorite: DbFavorite) {
        favoritesMapFlow.replayCache.lastOrNull()?.let { currentMap ->
            if (currentMap.containsKey(dbFavorite.id)) {
                favoritesDao.deleteFavorite(dbFavorite)
            } else {
                favoritesDao.addFavorite(dbFavorite)
            }
        } ?: run {
            favoritesDao.addFavorite(dbFavorite)
        }
    }
}