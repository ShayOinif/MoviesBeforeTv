package com.shayo.network

import com.shayo.network.BuildConfig.API_KEY
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface GenreNetworkService {
    @GET("genre/{type}/list")
    suspend fun getMoviesGenres(
        @Path("type")
        type: String,
        @Query("api_key")
        apiKey: String = API_KEY
    ): GenreNetworkResponse
}

data class GenreNetworkResponse(
    val genres: List<NetworkGenre>,
)