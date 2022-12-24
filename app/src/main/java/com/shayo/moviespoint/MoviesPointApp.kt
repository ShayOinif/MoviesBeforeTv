package com.shayo.moviespoint

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.shayo.moviespoint.common.snackbar.SnackBarManager
import com.shayo.moviespoint.home.HomeGraphRoutePattern
import com.shayo.moviespoint.home.homeGraph
import com.shayo.moviespoint.ui.theme.MoviesPointTheme
import kotlinx.coroutines.CoroutineScope



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesPointApp() {
    MoviesPointTheme {
        val appState = rememberAppState()

        // TODO: Maybe handle in app state
        var tab by rememberSaveable { mutableStateOf(HomeGraphRoutePattern) }

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
                        selected = tab == HomeGraphRoutePattern,
                        onClick = { tab = HomeGraphRoutePattern },
                        icon = { Icon(Icons.Default.Home, "Home Tab") },
                        label = { Text("Home") },
                        alwaysShowLabel = false,
                    )

                    NavigationBarItem(
                        selected = tab == "account",
                        onClick = { tab = "account" },
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
                moviesPointGraph(
                    //appState TODO:
                )
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
    //appState: MoviesPointAppState
) {
    homeGraph()
}