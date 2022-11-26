package com.shayo.movies

import com.shayo.network.NetworkMovieDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object MoviesModule {

    @Provides
    fun provideMoviesRepository(networkMovieDataSource: NetworkMovieDataSource): MoviesRepository =
        MoviesRepositoryImpl(networkMovieDataSource)

}