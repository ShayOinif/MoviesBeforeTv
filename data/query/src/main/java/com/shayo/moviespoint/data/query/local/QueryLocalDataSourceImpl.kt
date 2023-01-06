package com.shayo.moviespoint.data.query.local

import com.shayo.moviepoint.db.DbQuery
import com.shayo.moviepoint.db.QueryDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

// TODO: Map errors
internal class QueryLocalDataSourceImpl(
    private val queryDao: QueryDao,
) : QueryLocalDataSource {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val queryFlow = queryDao.getQueries().mapLatest { dbQueries ->
        Result.success(dbQueries)
    }.catch { error ->
        if (error !is CancellationException)
            emit(Result.failure(error))
    }.flowOn(Dispatchers.IO)

    override suspend fun add(dbQuery: DbQuery) = runCatching { queryDao.add(dbQuery) }

    override suspend fun deleteAll() = runCatching { queryDao.deleteAll() }

    private suspend fun runCatching(block: suspend () -> Unit) = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            block()
        }.fold(
            onSuccess = {
                Result.success(Unit)
            },
            onFailure = { error ->
                if (error is CancellationException)
                    throw error
                else
                    Result.failure(error)
            }
        )
    }
}