package com.shayo.moviepoint.db

import androidx.room.Entity

@Entity(tableName = "movie_category", primaryKeys = ["id", "category"])
data class MovieCategory(
    val id: Int,
    val category: String,
    val position: Int,
)