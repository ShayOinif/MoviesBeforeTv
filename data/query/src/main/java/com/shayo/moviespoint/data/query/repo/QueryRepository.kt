package com.shayo.moviespoint.data.query.repo

import com.shayo.moviespoint.data.query.model.Query
import kotlinx.coroutines.flow.Flow

interface QueryRepository {
    val queryFlow: Flow<Result<List<Query>>>

    suspend fun add(query: Query): Result<Unit>

    suspend fun deleteAll(): Result<Unit>
}