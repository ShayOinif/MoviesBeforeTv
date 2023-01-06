package com.shayo.moviepoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queries")
data class DbQuery(
    @PrimaryKey
    val query: String,
    val time: Long
)