package com.shayo.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

interface NetworkVideoDataSource {
    suspend fun getTrailer(type: String, movieId: Int): Result<List<NetworkVideo>>
}

internal class NetworkVideoDataSourceImpl constructor(
    private val videoNetworkService: VideoNetworkService
) : NetworkVideoDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getTrailer(type: String, movieId: Int): Result<List<NetworkVideo>> {
        return try {
            Result.success(videoNetworkService.getMoviesGenres(type, movieId).results.filter {
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