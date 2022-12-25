package com.shayo.moviespoint.search

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val SearchGraphRoutePattern = "search"
internal const val searchRoutePattern = "searchScreen"

fun NavGraphBuilder.searchGraph(
    //navController: NavController,
) {
    navigation(
        startDestination = searchRoutePattern,
        route = SearchGraphRoutePattern,
    ) {
        searchScreen()
    }
}

internal fun NavGraphBuilder.searchScreen() {
    composable(route =searchRoutePattern,) {
        SearchScreen()
    }
}