package com.shayo.moviespoint

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
fun MoviesPointApp(
    navOption: NavOption,
) {
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
                if (navOption == NavOption.BOTTOM_BAR) {
                    // TODO: Make one list of navigation options
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
            }
        ) { innerPaddingModifier ->
            when (navOption) {
                NavOption.NAV_RAIL -> {
                    Row {
                        NavigationRail(
                            modifier = Modifier.padding(innerPaddingModifier),
                            header = {
                                Icon(
                                    painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        ) {
                            NavigationRailItem(
                                selected = appState.tab == Tab.HOME,
                                onClick = {
                                    appState.navigate(HomeGraphRoutePattern)
                                },
                                icon = { Icon(Icons.Default.Home, "Home Tab") },
                                label = { Text("Home") },
                                alwaysShowLabel = false,
                            )

                            NavigationRailItem(
                                selected = appState.tab == Tab.SEARCH,
                                onClick = {
                                    appState.navigate(SearchGraphRoutePattern)
                                },
                                icon = { Icon(Icons.Default.Search, "Search Tab") },
                                label = { Text("Search") },
                                alwaysShowLabel = false,
                            )

                            NavigationRailItem(
                                selected = appState.tab == Tab.WATCHLIST,
                                onClick = {
                                    appState.navigate(WatchlistGraphRoutePattern)
                                },
                                icon = { Icon(Icons.Default.List, "Watchlist Tab") },
                                label = { Text("Watchlist") },
                                alwaysShowLabel = false,
                            )

                            NavigationRailItem(
                                selected = appState.tab == Tab.ACCOUNT,
                                onClick = {
                                    appState.navigate(AccountGraphRoutePattern)
                                },
                                icon = { Icon(Icons.Default.AccountCircle, "Account Tab") },
                                label = { Text("Account") },
                                alwaysShowLabel = false,
                            )
                        }

                        NavHost(
                            navController = appState.navController,
                            startDestination = HomeGraphRoutePattern,
                            modifier = Modifier.padding(innerPaddingModifier)
                        ) {
                            moviesPointGraph(appState)
                        }
                    }

                }
                NavOption.NAV_DRAWER -> {
                    PermanentNavigationDrawer(
                        //modifier = Modifier.padding(innerPaddingModifier).fillMaxSize(),
                        drawerContent = {
                            PermanentDrawerSheet(Modifier.width(240.dp)) {
                                NavigationDrawerItem(
                                    selected = appState.tab == Tab.HOME,
                                    onClick = {
                                        appState.navigate(HomeGraphRoutePattern)
                                    },
                                    icon = { Icon(Icons.Default.Home, "Home Tab") },
                                    label = { Text("Home") },
                                )

                                NavigationDrawerItem(
                                    selected = appState.tab == Tab.SEARCH,
                                    onClick = {
                                        appState.navigate(SearchGraphRoutePattern)
                                    },
                                    icon = { Icon(Icons.Default.Search, "Search Tab") },
                                    label = { Text("Search") },
                                )

                                NavigationDrawerItem(
                                    selected = appState.tab == Tab.WATCHLIST,
                                    onClick = {
                                        appState.navigate(WatchlistGraphRoutePattern)
                                    },
                                    icon = { Icon(Icons.Default.List, "Watchlist Tab") },
                                    label = { Text("Watchlist") },
                                )

                                NavigationDrawerItem(
                                    selected = appState.tab == Tab.ACCOUNT,
                                    onClick = {
                                        appState.navigate(AccountGraphRoutePattern)
                                    },
                                    icon = { Icon(Icons.Default.AccountCircle, "Account Tab") },
                                    label = { Text("Account") },
                                )
                            }
                        }
                    ) {
                        NavHost(
                            navController = appState.navController,
                            startDestination = HomeGraphRoutePattern,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            moviesPointGraph(appState)
                        }
                    }
                }
                else -> {
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

    watchlistGraph { mediaId, mediaType ->
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