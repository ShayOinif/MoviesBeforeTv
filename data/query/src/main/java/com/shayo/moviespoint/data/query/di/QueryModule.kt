package com.shayo.moviespoint.data.query.di

import com.shayo.moviepoint.db.QueryDao
import com.shayo.moviespoint.data.query.local.QueryLocalDataSourceImpl
import com.shayo.moviespoint.data.query.repo.QueryRepository
import com.shayo.moviespoint.data.query.repo.QueryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object QueryModule {
    @Provides
    fun provideQueryRepository(queryDao: QueryDao): QueryRepository =
        QueryRepositoryImpl(QueryLocalDataSourceImpl(queryDao))
}