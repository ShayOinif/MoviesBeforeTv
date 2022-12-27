package com.shayo.moviespoint.search

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val SearchGraphRoutePattern = "search"
internal const val searchRoutePattern = "searchScreen"

fun NavGraphBuilder.searchGraph(
    onMediaClicked: (mediaId: Int, mediaType: String) -> Unit,
    onPersonClicked: (personId: Int) -> Unit,
    //navController: NavController,
) {
    navigation(
        startDestination = searchRoutePattern,
        route = SearchGraphRoutePattern,
    ) {
        searchScreen(onMediaClicked, onPersonClicked)
    }
}

internal fun NavGraphBuilder.searchScreen(
    onMediaClicked: (mediaId: Int, mediaType: String) -> Unit,
    onPersonClicked: (personId: Int) -> Unit,
) {
    composable(route =searchRoutePattern,) {
        SearchScreen(onMediaClicked, onPersonClicked)
    }
}