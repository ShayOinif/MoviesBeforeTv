package com.shayo.movies

import com.shayo.network.NetworkVideoDataSource

interface VideoRepository {
    suspend fun getTrailer(movieId: Int): Result<Video?>
}

internal class VideoRepositoryImpl constructor(
    private val networkVideoDataSource: NetworkVideoDataSource
) : VideoRepository {
    override suspend fun getTrailer(movieId: Int): Result<Video?> {
        return networkVideoDataSource.getTrailer(movieId).map {
            it?.run {
                Video(key)
            }
        }
    }
}