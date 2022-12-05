package com.shayo.network

import retrofit2.http.GET
import retrofit2.http.Query

internal interface GenreNetworkService {
    @GET("genre/movie/list")
    suspend fun getMoviesGenres(
        @Query("api_key")
        apiKey: String = "3c91b35e1a662bf84c5f898ea35408c2"
    ): GenreNetworkResponse
}

data class GenreNetworkResponse(
    val genres: List<NetworkGenre>,
)