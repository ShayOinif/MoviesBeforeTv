package com.shayo.moviespoint

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController
import com.shayo.moviespoint.common.snackbar.SnackBarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Stable
class MoviesPointAppState(
    val navController: NavHostController,
    private val snackBarManager: SnackBarManager,
    val snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    init {
        coroutineScope.launch {
            snackBarManager.messages.filterNotNull().collect { message ->
                snackBarHostState.showSnackbar(message)
            }
        }
    }

    fun popUp() {
        navController.popBackStack()
    }

    fun navigate(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }
}