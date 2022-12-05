package com.shayo.moviepoint.db

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DbModule {
    @Provides
    @Singleton
    fun provideLocalFavoritesDataSource(
        @ApplicationContext
        context: Context,
    ): LocalFavoritesDataSource =
        LocalFavoritesDataSourceImpl(MovieDb.getDb(context).favoritesDao())

    @Provides
    @Singleton
    fun provideLocalMoviesDataSource(
        @ApplicationContext
        context: Context,
    ): LocalMoviesDataSource =
        LocalMoviesDataSourceImpl(MovieDb.getDb(context).moviesDao())

    @Provides
    @Singleton
    fun provideLocalMovieCategoryDataSource(
        @ApplicationContext
        context: Context,
    ): LocalMovieCategoryDataSource =
        LocalMovieCategoryDataSourceImpl(MovieDb.getDb(context).movieCategoryDao())
}