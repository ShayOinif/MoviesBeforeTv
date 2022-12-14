package com.shayo.movies

import com.shayo.network.NetworkVideoDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface VideoRepository {
    suspend fun getTrailers(type: String, movieId: Int): Result<List<Video>>
}

internal class VideoRepositoryImpl constructor(
    private val networkVideoDataSource: NetworkVideoDataSource
) : VideoRepository {
    override suspend fun getTrailers(type: String, movieId: Int): Result<List<Video>> {
        return withContext(Dispatchers.IO) {
            networkVideoDataSource.getTrailer(type, movieId).map {
                it.map { networkVideo ->
                    Video(networkVideo.key)
                }
            }
        }
    }
}