package com.shayo.movies

import com.shayo.moviepoint.db.DbFavorite
import com.shayo.moviepoint.db.LocalFavoritesDataSource
import com.shayo.moviespoint.firestore.FirestoreFavoritesDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

interface FavoritesRepository {
    fun setCollection(collectionName: String?)

    suspend fun toggleFavorite(id: Int, type: String)

    val favoritesMap: Flow<Map<Int, String>>
}

internal class FavoritesRepositoryImpl(
    private val localFavoritesDataSource: LocalFavoritesDataSource,
    private val firestoreFavoritesDataSource: FirestoreFavoritesDataSource,
) : FavoritesRepository {

    private val collectionName = MutableStateFlow<String?>(null)

    override fun setCollection(collectionName: String?) {
        this.collectionName.value = collectionName


        firestoreFavoritesDataSource.setCollection(collectionName)
    }

    override suspend fun toggleFavorite(id: Int, type: String) {
        collectionName.value?.let {
            firestoreFavoritesDataSource.toggleFavorite(id, type)
        } ?: localFavoritesDataSource.toggleFavorite(
            DbFavorite(id, type)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val favoritesMap = collectionName
        .flatMapLatest { collection ->
            collection?.run { firestoreFavoritesDataSource.favoritesMapFlow }
                ?: localFavoritesDataSource.favoritesMapFlow.map {
                    it.mapValues { (_, dbMovie) ->
                        dbMovie.type
                    }
                }
        }

}