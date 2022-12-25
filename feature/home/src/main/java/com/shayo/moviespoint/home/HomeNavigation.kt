package com.shayo.moviespoint.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val HomeGraphRoutePattern = "home"

fun NavGraphBuilder.homeGraph(
    onMediaClick: (mediaId: Int, mediaType: String) -> Unit,
) {
    navigation(
        startDestination = homeRoutePattern,
        route = HomeGraphRoutePattern,
    ) {
        homeScreen(
            onMediaClick = onMediaClick
        )
    }
}

internal const val homeRoutePattern = "homeScreen"

internal fun NavGraphBuilder.homeScreen(
    onMediaClick: (mediaId: Int, mediaType: String) -> Unit,
) {
    composable(route = homeRoutePattern) {
        HomeScreen(onMediaClick)
    }
}