package com.shayo.network

import com.google.gson.annotations.SerializedName
import com.shayo.network.BuildConfig.API_KEY
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// TODO: Change names cause it's not only movies
internal interface MovieNetworkService {
    @GET("{type}/{category}")
    suspend fun getMovies(
        @Path("type")
        type: String,
        @Path("category")
        category: String,
        @Query("page")
        page: Int = 1,
        @Query("api_key")
        apiKey: String = API_KEY,
    ): MovieNetworkResponse

    @GET("search/multi")
    suspend fun search(
        @Query("query")
        query: String,
        @Query("page")
        page: Int = 1,
        @Query("api_key")
        apiKey: String = API_KEY,
    ): MovieNetworkResponse

    @GET("{type}/{id}")
    suspend fun getById(
        @Path("type")
        type: String,
        @Path("id")
        id: Int,
        @Query("api_key")
        apiKey: String = API_KEY,
    ): NetworkQueryMovie
}

data class MovieNetworkResponse(
    val page: Int,
    val results: List<NetworkMovie>,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
)