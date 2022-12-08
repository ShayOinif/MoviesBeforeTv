package com.shayo.moviepoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class DbFavorite(
    @PrimaryKey
    val id: Int,
    val type: String
)