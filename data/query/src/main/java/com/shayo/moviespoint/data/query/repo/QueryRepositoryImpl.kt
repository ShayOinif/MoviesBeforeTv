package com.shayo.moviespoint.data.query.repo

import com.shayo.moviepoint.db.DbQuery
import com.shayo.moviespoint.data.query.local.QueryLocalDataSource
import com.shayo.moviespoint.data.query.model.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

// TODO: Map Errors
internal class QueryRepositoryImpl(
    private val queryLocalDataSource: QueryLocalDataSource
): QueryRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val queryFlow = queryLocalDataSource.queryFlow.mapLatest { result ->
        result.map { dbQueries ->
            dbQueries.map { dbQuery ->
                dbQuery.mapToDomain()
            }
        }
    }

    override suspend fun add(query: Query) = queryLocalDataSource.add(query.mapToDb())

    override suspend fun deleteAll() = queryLocalDataSource.deleteAll()
}

private fun Query.mapToDb() =
    DbQuery(query, time)

private fun DbQuery.mapToDomain() =
    Query(query, time)