package com.shayo.network

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import retrofit2.HttpException
import java.io.IOException

interface NetworkMovieDataSource {
    suspend fun getMovies(category: String, page: Int = 1): Result<MovieNetworkResponse>
}

internal class NetworkMovieDataSourceImpl constructor(
    private val movieNetworkService: MovieNetworkService,
) : NetworkMovieDataSource {

    override suspend fun getMovies(category: String, page: Int): Result<MovieNetworkResponse> {
        return try {
            Result.success(movieNetworkService.getMovies(category, page))
        } catch (ioException: IOException) {
            Result.failure(Exception("No Connection"))
        } catch (httpException: HttpException) {
            //Log.d("Shay", httpException.response()!!.errorBody()!!.string())
            Result.failure(
                Exception(
                    Json{ignoreUnknownKeys = true}.decodeFromString(
                        ServerError.serializer(),
                        httpException.response()!!.errorBody()!!.string()
                    ).message
                )
            )
        }
    }
}

@kotlinx.serialization.Serializable
private data class ServerError(
    @JsonNames("status_message")
    @SerializedName("status_message")
    val message: String,
)