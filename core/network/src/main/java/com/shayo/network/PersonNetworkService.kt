package com.shayo.network

import com.google.gson.annotations.SerializedName
import retrofit2.HttpException
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

interface PersonNetworkService {
    @GET("person/{id}")
    suspend fun getBioRet(
        @Path("id")
        id: Int,
        @Query("api_key")
        apiKey: String = BuildConfig.API_KEY,
        @Query("append_to_response")
        append: String = "combined_credits",
    ): PersonNetworkServiceResponse
}

suspend fun PersonNetworkService.getBio(id: Int) =
    try {
        Result.success(getBioRet(id = id))
    } catch (ioException: IOException) {
        Result.failure(Exception("No Connection"))
    } catch (httpException: HttpException) {
        Result.failure(
            Exception(
                JsonSerializer.getJsonSerializer().decodeError(httpException)
            )
        )
    }

// TODO: Add birthday, death day, gender, place of birth,
data class PersonNetworkServiceResponse(
    val id: Int,
    val biography: String,
    val name: String,
    @SerializedName("profile_path")
    val profilePath: String?,
    @SerializedName("combined_credits")
    val combinedCredits: NetworkCombinedCredits,
)

data class NetworkCombinedCredits(
    val cast: List<NetworkMovie<Int>>,
    val crew: List<NetworkMovie<Int>>,
)