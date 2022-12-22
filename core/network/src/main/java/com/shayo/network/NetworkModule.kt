package com.shayo.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Singleton
    @Provides
    internal fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    fun provideNetworkMovieDataSource(retrofit: Retrofit): NetworkMovieDataSource =
        NetworkMovieDataSourceImpl(
            retrofit.create(MovieNetworkService::class.java)
        )

    @Provides
    fun provideNetworkGenreDataSource(retrofit: Retrofit): NetworkGenreDataSource =
        NetworkGenreDataSourceImpl(
            retrofit.create(GenreNetworkService::class.java)
        )

    @Provides
    fun provideNetworkVideoDataSource(retrofit: Retrofit): NetworkVideoDataSource =
        NetworkVideoDataSourceImpl(
            retrofit.create(VideoNetworkService::class.java)
        )

    @Provides
    fun provideNetworkCreditsDataSource(retrofit: Retrofit): NetworkCreditsDataSource =
        NetworkCreditsDataSourceImpl(
            retrofit.create(CreditsNetworkService::class.java)
        )

    @Provides
    fun providePersonNetworkService(retrofit: Retrofit): PersonNetworkService =
        retrofit.create(PersonNetworkService::class.java)
}