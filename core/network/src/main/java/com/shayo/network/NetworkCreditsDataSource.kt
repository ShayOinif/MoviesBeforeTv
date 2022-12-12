package com.shayo.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

interface NetworkCreditsDataSource {
    suspend fun getCredits(type: String, id: Int): Result<CreditsServiceResponse>
}

class NetworkCreditsDataSourceImpl(
    private val creditsNetworkService: CreditsNetworkService,
) : NetworkCreditsDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCredits(type: String, id: Int): Result<CreditsServiceResponse> {
        return try {
            Result.success(creditsNetworkService.getCredits(type, id))
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