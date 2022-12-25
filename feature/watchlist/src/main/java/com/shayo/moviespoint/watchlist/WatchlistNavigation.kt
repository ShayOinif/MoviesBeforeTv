package com.shayo.moviespoint.watchlist

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val WatchlistGraphRoutePattern = "watchlist"

fun NavGraphBuilder.watchlistGraph(
    onMediaClick: (mediaId: Int, mediaType: String) -> Unit,
) {
    navigation(
        startDestination = watchlistRoutePattern,
        route = WatchlistGraphRoutePattern,
    ) {
        watchlistScreen(
            onMediaClick = onMediaClick,
        )
    }
}

internal const val watchlistRoutePattern = "watchlistScreen"

internal fun NavGraphBuilder.watchlistScreen(
    onMediaClick: (mediaId: Int, mediaType: String) -> Unit,
) {
    composable(route = watchlistRoutePattern) {
        WatchlistScreen(onMediaClick)
    }
}