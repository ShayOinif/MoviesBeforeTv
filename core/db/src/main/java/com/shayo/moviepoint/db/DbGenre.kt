package com.shayo.moviepoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
data class DbGenre(
    @PrimaryKey
    val id: Int,
    val name: String,
)