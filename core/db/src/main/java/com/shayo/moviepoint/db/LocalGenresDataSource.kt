package com.shayo.moviepoint.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

interface LocalGenresDataSource {
    val genresFlow: Flow<List<DbGenre>>

    suspend fun addGenres(dbGenre: List<DbGenre>)
}

internal class LocalGenresDataSourceImpl(
    private val genresDao: GenresDao,
) : LocalGenresDataSource {

    private val scope = CoroutineScope(SupervisorJob())

    override val genresFlow = genresDao.getGenres()
        .shareIn(scope, SharingStarted.WhileSubscribed(1_500), 1)

    override suspend fun addGenres(dbGenre: List<DbGenre>) {
        genresDao.addGenres(dbGenre)
    }
}