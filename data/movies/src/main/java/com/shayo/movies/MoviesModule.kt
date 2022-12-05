package com.shayo.movies

import com.shayo.moviepoint.db.LocalFavoritesDataSource
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
        localFavoritesDataSource: LocalFavoritesDataSource,
    ): MoviesRepository =
        MoviesRepositoryImpl(networkMovieDataSource, localFavoritesDataSource)

    @Provides
    fun provideGenresRepository(networkGenreDataSource: NetworkGenreDataSource): GenreRepository =
        GenreRepositoryImpl(networkGenreDataSource)

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