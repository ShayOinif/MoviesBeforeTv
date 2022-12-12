package com.shayo.network

import com.shayo.network.BuildConfig.API_KEY
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CreditsNetworkService {
    @GET("{type}/{id}/credits")
    suspend fun getCredits(
        @Path("type")
        type: String,
        @Path("id")
        id: Int,
        @Query("api_key")
        apiKey: String = API_KEY,
    ): CreditsServiceResponse
}

data class CreditsServiceResponse(
    val cast: List<NetworkCredit>,
    val crew: List<NetworkCredit>
)