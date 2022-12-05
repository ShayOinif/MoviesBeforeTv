package com.shayo.network

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames

// TODO: In genres the serialization is incorrect
@kotlinx.serialization.Serializable
internal data class ServerError @OptIn(ExperimentalSerializationApi::class) constructor(
    @JsonNames("status_message")
    @SerializedName("status_message")
    val message: String,
)