package com.shayo.moviespoint.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val HomeGraphRoutePattern = "home"

fun NavGraphBuilder.homeGraph(
    //navController: NavController,
) {
    navigation(
        startDestination = homeRoutePattern,
        route = HomeGraphRoutePattern,
    ) {
        homeScreen()
    }
}

internal const val homeRoutePattern = "homeScreen"

internal fun NavGraphBuilder.homeScreen() {
    composable(route = homeRoutePattern) {
        HomeScreen()
    }
}/*

internal const val contactDetailRoute = "contact"
internal const val contactDetailRouteParam = "lookupKey"

internal fun NavController.navigateToContactDetails(lookupKey: String) {
    navigate("$contactDetailRoute/$lookupKey")
}

internal fun NavGraphBuilder.contactDetailScreen(
    navigateBack: () -> Unit,
) {
    composable(route = "$contactDetailRoute/{$contactDetailRouteParam}",
        arguments = listOf(
            navArgument(
                name = contactDetailRouteParam,
            ) {
                type = NavType.StringType
            }
        )) {
        val lookupKey = it.arguments?.getString(contactDetailRouteParam) ?: "-1"

        ContactDetailScreen(
            lookupKey = lookupKey,
            navigateBack = navigateBack
        )
    }
}*/