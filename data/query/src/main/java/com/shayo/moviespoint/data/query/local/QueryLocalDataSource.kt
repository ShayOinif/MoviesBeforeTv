package com.shayo.moviespoint.data.query.local

import com.shayo.moviepoint.db.DbQuery
import kotlinx.coroutines.flow.Flow

internal interface QueryLocalDataSource {
    val queryFlow: Flow<Result<List<DbQuery>>>

    suspend fun add(dbQuery: DbQuery): Result<Unit>

    suspend fun deleteAll(): Result<Unit>
}