package com.shayo.moviespoint.firestore

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirestoreModule {
    @Provides
    @Singleton
    fun provideMixedFavoritesDataSource(): MixedFavoritesDataSource =
        MixedFavoritesDataSourceImpl()
}