package com.shayo.moviepoint.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DbMovie::class], version = 1)
internal abstract class MovieDb : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao

    abstract fun moviesDao(): MoviesDao

    abstract fun movieCategoryDao(): MovieCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDb? = null

        fun getDb(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    MovieDb::class.java, "movie-db"
                ).build()
            }

    }
}
