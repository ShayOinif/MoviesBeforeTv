package com.shayo.moviespoint.account

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val AccountGraphRoutePattern = "account"

fun NavGraphBuilder.accountGraph(
    //navController: NavController,
) {
    navigation(
        startDestination = accountRoutePattern,
        route = AccountGraphRoutePattern,
    ) {
        homeScreen()
    }
}

internal const val accountRoutePattern = "accountScreen"

internal fun NavGraphBuilder.homeScreen() {
    composable(route = accountRoutePattern) {
        AccountScreen()
    }
}