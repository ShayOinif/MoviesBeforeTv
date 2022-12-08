package com.shayo.moviepoint.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(dbFavorite: DbFavorite)

    @Delete
    suspend fun removeFavorite(dbFavorite: DbFavorite)

    @Query("SELECT * FROM favorites")
    fun favoritesMapFlow(): Flow<List<DbFavorite>>

    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getFavoriteById(id: Int): DbFavorite?
}