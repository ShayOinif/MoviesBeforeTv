package com.shayo.moviespoint.account

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val AccountGraphRoutePattern = "account"

fun NavGraphBuilder.accountGraph(
    //navController: NavController,
    postMessageSnackBar: (message: String) -> Unit,
) {
    navigation(
        startDestination = accountRoutePattern,
        route = AccountGraphRoutePattern,
    ) {
        homeScreen(postMessageSnackBar)
    }
}

internal const val accountRoutePattern = "accountScreen"

internal fun NavGraphBuilder.homeScreen(
    postMessageSnackBar: (message: String) -> Unit,
) {
    composable(route = accountRoutePattern) {
        AccountScreen(postMessageSnackBar)
    }
}