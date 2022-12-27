package com.shayo.moviespoint.common.snackbar

import kotlinx.coroutines.flow.MutableStateFlow

object SnackBarManager {
    val messages: MutableStateFlow<SnackBarMessage?> = MutableStateFlow(null)
}

sealed class SnackBarMessage(
    val message: String,
    val perm: Boolean = false
) {
    class RegularMessage(message: String) : SnackBarMessage(message, false)

    sealed class NetworkMessage(message: String, val dismiss: Boolean) : SnackBarMessage(message, true) {
        object HasNetwork : NetworkMessage("", true)

        class NoNetwork(message: String) : NetworkMessage(message, false)
    }
}