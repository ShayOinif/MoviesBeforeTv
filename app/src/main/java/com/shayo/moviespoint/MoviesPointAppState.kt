package com.shayo.moviespoint

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.shayo.moviespoint.account.AccountGraphRoutePattern
import com.shayo.moviespoint.common.snackbar.SnackBarManager
import com.shayo.moviespoint.common.snackbar.SnackBarMessage
import com.shayo.moviespoint.home.HomeGraphRoutePattern
import com.shayo.moviespoint.search.SearchGraphRoutePattern
import com.shayo.moviespoint.watchlist.WatchlistGraphRoutePattern
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
    var tab by mutableStateOf(Tab.HOME)
        private set

    init {
        coroutineScope.launch {
            launch {
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
            launch {
                navController.currentBackStackEntryFlow.collectLatest {
                    val last = navController.backQueue.last { backEntry ->
                        backEntry.destination.route?.contains(HomeGraphRoutePattern) == true ||
                                backEntry.destination.route?.contains(WatchlistGraphRoutePattern) == true ||
                                backEntry.destination.route?.contains(AccountGraphRoutePattern) == true ||
                                backEntry.destination.route?.contains(SearchGraphRoutePattern) == true
                    }.destination.route ?: ""

                    when {
                        last.contains(HomeGraphRoutePattern) -> tab = Tab.HOME
                        last.contains(WatchlistGraphRoutePattern) -> tab = Tab.WATCHLIST
                        last.contains(AccountGraphRoutePattern) -> tab = Tab.ACCOUNT
                        last.contains(SearchGraphRoutePattern) -> tab = Tab.SEARCH
                    }
                }
            }
        }
    }

    /*fun popUp() {
        navController.popBackStack()
    }*/

    fun navigate(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { // TODO: There are some bugs when popping while home screen has another dest above
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

enum class Tab { HOME, WATCHLIST, ACCOUNT, SEARCH }