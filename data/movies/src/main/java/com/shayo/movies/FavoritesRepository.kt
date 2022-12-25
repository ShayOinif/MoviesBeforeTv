package com.shayo.movies

import com.shayo.moviepoint.db.DbFavorite
import com.shayo.moviepoint.db.LocalFavoritesDataSource
import com.shayo.moviespoint.firestore.FirestoreFavoritesDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

interface FavoritesRepository {
    fun setCollection(collectionName: String?)

    suspend fun toggleFavorite(id: Int, type: String)

    val favoritesMap: Flow<Map<Int, String>>

    val collName: StateFlow<String?>

    suspend fun getAllFavoritesIds(collectionName: String?): List<Int>
}

internal class FavoritesRepositoryImpl(
    private val localFavoritesDataSource: LocalFavoritesDataSource,
    private val firestoreFavoritesDataSource: FirestoreFavoritesDataSource,
) : FavoritesRepository {

    private val collectionName = MutableStateFlow<String?>(null)
    override val collName = collectionName.asStateFlow()

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

    override suspend fun getAllFavoritesIds(collectionName: String?): List<Int> {
        val ids = mutableListOf<Int>()

        collectionName?.let {
            ids.addAll(firestoreFavoritesDataSource.getFavoritesIdsByCollection(collectionName))
        }

        ids.addAll(localFavoritesDataSource.getFavoritesIds())

        return ids
    }
}