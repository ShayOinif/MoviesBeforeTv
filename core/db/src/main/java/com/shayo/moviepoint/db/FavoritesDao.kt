package com.shayo.moviepoint.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FavoritesDao {
    @Query("SELECT * FROM favorites")
    fun getFavoritesPaging(): PagingSource<Int, DbFavorite>

    @Query("SELECT id FROM favorites")
    fun getFavoritesMap(): Flow<List<Int>>

    @Insert
    suspend fun addFavorite(dbMovie: DbFavorite)

    @Delete
    suspend fun deleteFavorite(dbMovie: DbFavorite)
}