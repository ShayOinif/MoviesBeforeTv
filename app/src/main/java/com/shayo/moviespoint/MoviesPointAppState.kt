package com.shayo.moviespoint

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.shayo.moviespoint.common.snackbar.SnackBarManager
import com.shayo.moviespoint.common.snackbar.SnackBarMessage
import com.shayo.moviespoint.home.HomeGraphRoutePattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Stable
class MoviesPointAppState(
    val navController: NavHostController,
    private val snackBarManager: SnackBarManager,
    val snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    init {
        coroutineScope.launch {
            snackBarManager.messages.filterNotNull().collectLatest { message ->
                when (message) {
                    is SnackBarMessage.NetworkMessage -> {
                        if (message.dismiss) {
                            snackBarHostState.currentSnackbarData?.dismiss()
                        } else {
                            snackBarHostState.showSnackbar(
                                message.message,
                                if (message.perm) {
                                    ""
                                } else null,
                                message.perm
                            )
                        }
                    }
                    else -> {
                        snackBarHostState.showSnackbar(message.message)
                    }
                }
            }
        }
    }

    fun popup() {
        navigate(HomeGraphRoutePattern)
    }

    fun navigate(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun postSnackBarMessage(message: String) {
        snackBarManager.messages.value = SnackBarMessage.RegularMessage(message)
    }
}