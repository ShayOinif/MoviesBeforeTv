package com.shayo.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoNetworkService {
    @GET("movie/{movie_id}/videos")
    suspend fun getMoviesGenres(
        @Path("movie_id")
        movieId: Int,
        @Query("api_key")
        apiKey: String = "3c91b35e1a662bf84c5f898ea35408c2"
    ): VideoServiceResponse
}

data class VideoServiceResponse(
    val results: List<NetworkVideo>
)