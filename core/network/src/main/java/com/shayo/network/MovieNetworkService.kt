package com.shayo.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface MovieNetworkService {
    @GET("movie/{category}")
    suspend fun getMovies(
        @Path("category")
        category: String,
        @Query("page")
        page: Int = 1,
        @Query("api_key")
        apiKey: String = "3c91b35e1a662bf84c5f898ea35408c2"
    ): MovieNetworkResponse

    @GET("search/movie")
    suspend fun search(
        @Query("query")
        query: String,
        @Query("page")
        page: Int = 1,
        @Query("api_key")
        apiKey: String = "3c91b35e1a662bf84c5f898ea35408c2"
    ): MovieNetworkResponse
}

data class MovieNetworkResponse(
    val page: Int,
    val results: List<NetworkMovie>,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
)