package com.shayo.moviespoint.common.snackbar

import kotlinx.coroutines.flow.MutableStateFlow

object SnackBarManager {
    val messages: MutableStateFlow<String?> = MutableStateFlow(null)
}