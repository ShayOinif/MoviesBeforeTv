package com.shayo.moviepoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "programs")
data class DbProgram(
    @PrimaryKey
    val id: Long,
)