package com.shayo.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull.serializer
import retrofit2.HttpException
import java.io.IOException

interface NetworkMovieDataSource {
    suspend fun getMovies(category: String, page: Int = 1): Result<MovieNetworkResponse>

    suspend fun searchMovies(query: String, page: Int = 1): Result<MovieNetworkResponse>
}

internal class NetworkMovieDataSourceImpl constructor(
    private val movieNetworkService: MovieNetworkService,
) : NetworkMovieDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getMovies(category: String /* TODO */, page: Int): Result<MovieNetworkResponse> {
        return try {
            Result.success(movieNetworkService.getMovies(category, page))
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

    override suspend fun searchMovies(query: String, page: Int): Result<MovieNetworkResponse> {
        return try {
            Result.success(movieNetworkService.search(query, page))
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