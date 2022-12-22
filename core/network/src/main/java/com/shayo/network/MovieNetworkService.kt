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
    ): MovieNetworkResponse<Int>

    @GET("search/multi")
    suspend fun search(
        @Query("query")
        query: String,
        @Query("page")
        page: Int = 1,
        @Query("api_key")
        apiKey: String = API_KEY,
    ): MovieNetworkResponse<Int>

    @GET("{type}/{id}")
    suspend fun getById(
        @Path("type")
        type: String,
        @Path("id")
        id: Int,
        @Query("api_key")
        apiKey: String = API_KEY,
    ): NetworkMovie<NetworkGenre>

    @GET("discover/{type}")
    suspend fun discover(
        @Path("type")
        type: String,
        @Query("api_key")
        apiKey: String = API_KEY,
    ): MovieNetworkResponse<Int>
}

/* TODO: We use this class also for search, which gives us persons as well, so find a way to make it
 *  more generic or composed from different classes
*/
data class MovieNetworkResponse<T>(
    val page: Int,
    val results: List<NetworkMovie<T>>,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
)