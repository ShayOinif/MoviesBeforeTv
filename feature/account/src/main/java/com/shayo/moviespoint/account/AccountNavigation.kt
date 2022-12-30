package com.shayo.moviespoint.account

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val AccountGraphRoutePattern = "account"

fun NavGraphBuilder.accountGraph(
    popup: () -> Unit,
    postMessageSnackBar: (message: String) -> Unit,
) {
    navigation(
        startDestination = accountRoutePattern,
        route = AccountGraphRoutePattern,
    ) {
        homeScreen(popup, postMessageSnackBar)
    }
}

internal const val accountRoutePattern = "accountScreen"

internal fun NavGraphBuilder.homeScreen(
    popup: () -> Unit,
    postMessageSnackBar: (message: String) -> Unit,
) {
    composable(route = accountRoutePattern) {
        BackHandler { popup() }

        AccountScreen(postMessageSnackBar)
    }
}