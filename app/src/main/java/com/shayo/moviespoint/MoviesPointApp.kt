package com.shayo.moviespoint

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.shayo.moviespoint.account.AccountGraphRoutePattern
import com.shayo.moviespoint.account.accountGraph
import com.shayo.moviespoint.common.snackbar.SnackBarManager
import com.shayo.moviespoint.home.HomeGraphRoutePattern
import com.shayo.moviespoint.home.homeGraph
import com.shayo.moviespoint.mediadetail.mediaDetailGraph
import com.shayo.moviespoint.mediadetail.navigateToMediaDetail
import com.shayo.moviespoint.personfeature.navigateToPerson
import com.shayo.moviespoint.personfeature.personGraph
import com.shayo.moviespoint.search.SearchGraphRoutePattern
import com.shayo.moviespoint.search.searchGraph
import com.shayo.moviespoint.ui.theme.MoviesPointTheme
import com.shayo.moviespoint.watchlist.WatchlistGraphRoutePattern
import com.shayo.moviespoint.watchlist.watchlistGraph
import kotlinx.coroutines.CoroutineScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesPointApp() {
    MoviesPointTheme {
        val appState = rememberAppState()

        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = appState.snackBarHostState,
                    modifier = Modifier.padding(8.dp),
                    snackbar = { snackBarData ->
                        Snackbar(snackBarData)
                    }
                )
            },
            bottomBar = {
                // TODO: Handle different window sizes

                NavigationBar {
                    NavigationBarItem(
                        selected = appState.tab == Tab.HOME,
                        onClick = {
                            appState.navigate(HomeGraphRoutePattern)
                        },
                        icon = { Icon(Icons.Default.Home, "Home Tab") },
                        label = { Text("Home") },
                        alwaysShowLabel = false,
                    )

                    NavigationBarItem(
                        selected = appState.tab == Tab.SEARCH,
                        onClick = {
                            appState.navigate(SearchGraphRoutePattern)
                        },
                        icon = { Icon(Icons.Default.Search, "Search Tab") },
                        label = { Text("Search") },
                        alwaysShowLabel = false,
                    )

                    NavigationBarItem(
                        selected = appState.tab == Tab.WATCHLIST,
                        onClick = {
                            appState.navigate(WatchlistGraphRoutePattern)
                        },
                        icon = { Icon(Icons.Default.List, "Watchlist Tab") },
                        label = { Text("Watchlist") },
                        alwaysShowLabel = false,
                    )

                    NavigationBarItem(
                        selected = appState.tab == Tab.ACCOUNT,
                        onClick = {
                            appState.navigate(AccountGraphRoutePattern)
                        },
                        icon = { Icon(Icons.Default.AccountCircle, "Account Tab") },
                        label = { Text("Account") },
                        alwaysShowLabel = false,
                    )
                }
            }
        ) { innerPaddingModifier ->
            NavHost(
                navController = appState.navController,
                startDestination = HomeGraphRoutePattern,
                modifier = Modifier.padding(innerPaddingModifier)
            ) {
                moviesPointGraph(appState)
            }
        }
    }
}

@Composable
private fun rememberAppState(
    navController: NavHostController = rememberNavController(),
    snackBarManager: SnackBarManager = SnackBarManager,
    snackBarHostState: SnackbarHostState = SnackbarHostState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(navController, snackBarManager, coroutineScope) {
        MoviesPointAppState(navController, snackBarManager, snackBarHostState, coroutineScope)
    }

private fun NavGraphBuilder.moviesPointGraph(
    appState: MoviesPointAppState
) {
    homeGraph { mediaId, mediaType ->
        appState.navController.navigateToMediaDetail(mediaId, mediaType)
    }

    watchlistGraph{ mediaId, mediaType ->
        appState.navController.navigateToMediaDetail(mediaId, mediaType)
    }

    searchGraph(
        appState.navController::navigateToMediaDetail,
        appState.navController::navigateToPerson
    )

    accountGraph(appState::postSnackBarMessage)

    mediaDetailGraph { personId ->
        appState.navController.navigateToPerson(personId)
    }

    personGraph { mediaId, mediaType ->
        appState.navController.navigateToMediaDetail(mediaId, mediaType)
    }
}