package com.shayo.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull.serializer
import retrofit2.HttpException
import java.io.IOException

interface NetworkVideoDataSource {
    suspend fun getTrailer(movieId: Int): Result<NetworkVideo?>
}

internal class NetworkVideoDataSourceImpl constructor(
    private val videoNetworkService: VideoNetworkService
) : NetworkVideoDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getTrailer(movieId: Int): Result<NetworkVideo?> {
        return try {
            Result.success(videoNetworkService.getMoviesGenres(movieId).results.firstOrNull {
                it.official && it.type == "Trailer" && it.site == "YouTube"
            })
        } catch (ioException: IOException) {
            Result.failure(Exception("No Connection"))
        } catch (httpException: HttpException) {
            Result.failure(
                Exception(
                    json.decodeFromString(
                        ServerError.serializer(),
                        httpException.response()!!.errorBody()!!.string()
                    ).message
                )
            )
        }
    }
}