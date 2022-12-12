package com.shayo.network

import com.google.gson.annotations.SerializedName

data class NetworkCredit(
    val id: Int,
    val name: String,
    @SerializedName("profile_path")
    val profilePath: String?,
    @SerializedName("character", alternate = ["job"])
    val description: String,
)