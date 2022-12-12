package com.shayo.movies

import com.shayo.moviepoint.db.LocalFavoritesDataSource
import com.shayo.moviepoint.db.LocalGenresDataSource
import com.shayo.moviepoint.db.LocalMovieCategoryDataSource
import com.shayo.moviepoint.db.LocalMoviesDataSource
import com.shayo.moviespoint.firestore.FirestoreFavoritesDataSource
import com.shayo.network.NetworkCreditsDataSource
import com.shayo.network.NetworkGenreDataSource
import com.shayo.network.NetworkMovieDataSource
import com.shayo.network.NetworkVideoDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object MoviesModule {

    @Provides
    fun provideUserRepository(): UserRepository = UserRepositoryImpl()

    @Singleton
    @Provides
    fun provideFavoritesRepository(
        firestoreFavoritesDataSource: FirestoreFavoritesDataSource,
        localFavoritesDataSource: LocalFavoritesDataSource,
    ): FavoritesRepository =
        FavoritesRepositoryImpl(localFavoritesDataSource, firestoreFavoritesDataSource,)

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
        genreRepository: GenreRepository,
        favoritesRepository: FavoritesRepository,
    ): MovieManager = MovieManagerImpl(moviesRepository, genreRepository, favoritesRepository)

    @Provides
    fun provideVideoRepository(
        networkVideoDataSource: NetworkVideoDataSource
    ): VideoRepository = VideoRepositoryImpl(networkVideoDataSource)

    @Provides
    fun provideCreditsRepository(
        networkCreditsDataSource: NetworkCreditsDataSource,
    ): CreditsRepository = CreditsRepositoryImpl(networkCreditsDataSource)
}