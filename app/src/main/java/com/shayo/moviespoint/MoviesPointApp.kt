package com.shayo.moviespoint

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shayo.moviespoint.account.accountGraph
import com.shayo.moviespoint.common.snackbar.SnackBarManager
import com.shayo.moviespoint.home.HomeGraphRoutePattern
import com.shayo.moviespoint.home.homeGraph
import com.shayo.moviespoint.search.searchGraph
import com.shayo.moviespoint.ui.theme.MoviesPointTheme
import com.shayo.moviespoint.watchlist.watchlistGraph
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesPointApp(
    navOption: NavOption,
) {
    MoviesPointTheme {
        val appState = rememberAppState()

        val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

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
                    NavigationBar {
                        screens.forEach { moviesPointScreen ->
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == moviesPointScreen.route } == true,
                                onClick = {
                                    appState.navigate(moviesPointScreen.route)
                                },
                                icon = { Icon(moviesPointScreen.icon, null) },
                                label = { Text(moviesPointScreen.label) },
                                alwaysShowLabel = false,
                            )
                        }
                    }
                }
            }
        ) { innerPaddingModifier ->
            when (navOption) {
                NavOption.NAV_RAIL -> {
                    Row {
                        NavigationRail(
                            header = {
                                Icon(
                                    painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        ) {
                            screens.forEach { moviesPointScreen ->
                                NavigationRailItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == moviesPointScreen.route } == true,
                                    onClick = {
                                        appState.navigate(moviesPointScreen.route)
                                    },
                                    icon = { Icon(moviesPointScreen.icon, null) },
                                    label = { Text(moviesPointScreen.label) },
                                    alwaysShowLabel = false,
                                )
                            }
                        }

                        MoviesPointNavHost(appState)
                    }

                }
                NavOption.NAV_DRAWER -> {
                    PermanentNavigationDrawer(
                        drawerContent = {
                            PermanentDrawerSheet(
                                modifier = Modifier.width(180.dp),
                                drawerTonalElevation = 6.dp,
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )

                                screens.forEach { moviesPointScreen ->
                                    NavigationDrawerItem(
                                        selected = currentDestination?.hierarchy?.any { it.route == moviesPointScreen.route } == true,
                                        onClick = {
                                            appState.navigate(moviesPointScreen.route)
                                        },
                                        icon = { Icon(moviesPointScreen.icon, null) },
                                        label = { Text(moviesPointScreen.label) },
                                    )
                                }
                            }
                        }
                    ) {
                        MoviesPointNavHost(
                            appState = appState,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                else -> {
                    MoviesPointNavHost(
                        appState = appState,
                        modifier = Modifier.padding(innerPaddingModifier),
                    )
                }
            }
        }
    }
}

@Composable
fun MoviesPointNavHost(
    appState: MoviesPointAppState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = appState.navController,
        startDestination = HomeGraphRoutePattern,
        modifier = modifier
    ) {
        moviesPointGraph(appState)
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
    appState: MoviesPointAppState,
) {
    homeGraph(appState.navController)

    watchlistGraph(appState::popup, appState.navController)

    searchGraph(appState::popup, appState.navController)

    accountGraph(appState::popup, appState::postSnackBarMessage)
}