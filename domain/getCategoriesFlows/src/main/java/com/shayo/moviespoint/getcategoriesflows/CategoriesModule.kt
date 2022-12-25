package com.shayo.moviespoint.getcategoriesflows

import com.shayo.movies.MovieManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object CategoriesModule {

    @Provides
    fun provideGetCategoriesFlowsUseCase(
        movieManager: MovieManager,
    ): GetCategoriesFlowsUseCase = GetCategoriesFlowsUseCaseImpl(movieManager)
}