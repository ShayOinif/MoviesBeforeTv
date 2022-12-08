package com.shayo.movies

import com.shayo.moviepoint.db.LocalGenresDataSource
import com.shayo.moviepoint.db.LocalMovieCategoryDataSource
import com.shayo.moviepoint.db.LocalMoviesDataSource
import com.shayo.network.NetworkGenreDataSource
import com.shayo.network.NetworkMovieDataSource
import com.shayo.network.NetworkVideoDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object MoviesModule {

    @Provides
    fun provideMoviesRepository(
        networkMovieDataSource: NetworkMovieDataSource,
        localMovieCategoryDataSource: LocalMovieCategoryDataSource,
        localMoviesDataSource: LocalMoviesDataSource,
    ): MoviesRepository =
        MoviesRepositoryImpl(networkMovieDataSource, localMovieCategoryDataSource, localMoviesDataSource)

    @Provides
    fun provideGenresRepository(networkGenreDataSource: NetworkGenreDataSource, localGenresDataSource: LocalGenresDataSource): GenreRepository =
        GenreRepositoryImpl(networkGenreDataSource, localGenresDataSource)

    @Provides
    fun provideMovieManager(
        moviesRepository: MoviesRepository,
        genreRepository: GenreRepository
    ): MovieManager = MovieManagerImpl(moviesRepository, genreRepository)

    @Provides
    fun provideVideoRepository(
        networkVideoDataSource: NetworkVideoDataSource
    ): VideoRepository = VideoRepositoryImpl(networkVideoDataSource)
}