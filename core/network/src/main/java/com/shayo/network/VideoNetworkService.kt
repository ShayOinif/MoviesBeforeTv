package com.shayo.network

import com.shayo.network.BuildConfig.API_KEY
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoNetworkService {
    @GET("{type}/{movie_id}/videos")
    suspend fun getMoviesGenres(
        @Path("type")
        type: String,
        @Path("movie_id")
        movieId: Int,
        @Query("api_key")
        apiKey: String = API_KEY
    ): VideoServiceResponse
}

data class VideoServiceResponse(
    val results: List<NetworkVideo>
)