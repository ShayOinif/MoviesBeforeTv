package com.shayo.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

interface NetworkGenreDataSource {
    suspend fun getMoviesGenres(): Result<List<NetworkGenre>>
}

internal class NetworkGenreDataSourceImpl constructor(
    private val genreNetworkService: GenreNetworkService
) : NetworkGenreDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getMoviesGenres(): Result<List<NetworkGenre>>{
        return try {

            val movies = genreNetworkService.getMoviesGenres("movie").genres
            val shows = genreNetworkService.getMoviesGenres("tv").genres

            Result.success(movies.plus(shows))
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