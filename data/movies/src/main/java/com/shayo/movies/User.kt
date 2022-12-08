package com.shayo.movies

import android.net.Uri

data class User(
    val displayName: String,
    val email: String,
    val photoUrl: Uri?,
)