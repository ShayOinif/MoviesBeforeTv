package com.shayo.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException

// TODO: Make all services use this singleton
internal class JsonSerializer private constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    fun decodeError(httpException: HttpException) = json.decodeFromString(
        ServerError.serializer(),
        httpException.response()!!.errorBody()!!.string()
    ).message

    companion object {
        var INSTANCE: JsonSerializer? = null

        fun getJsonSerializer() =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: JsonSerializer().also { INSTANCE = it }
            }
    }
}