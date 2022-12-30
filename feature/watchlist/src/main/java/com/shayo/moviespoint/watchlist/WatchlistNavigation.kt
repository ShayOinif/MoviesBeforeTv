package com.shayo.moviespoint.watchlist

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.shayo.moviespoint.mediadetail.mediaDetailScreen
import com.shayo.moviespoint.mediadetail.navigateToMediaDetail
import com.shayo.moviespoint.personfeature.navigateToPerson
import com.shayo.moviespoint.personfeature.personScreen
import com.shayo.moviespoint.ui.DetailsOrigin

const val WatchlistGraphRoutePattern = "watchlist"

fun NavGraphBuilder.watchlistGraph(
    popup: () -> Unit,
    navController: NavHostController,
) {
    navigation(
        startDestination = watchlistRoutePattern,
        route = WatchlistGraphRoutePattern,
    ) {
        watchlistScreen(popup) { mediaId: Int, mediaType: String, detailsOrigin ->
            navController.navigateToMediaDetail(
                mediaId,
                mediaType,
                watchlistRoutePattern,
                detailsOrigin,
            )
        }

        mediaDetailScreen(
            route = watchlistRoutePattern,
            personClick = { personId ->
                navController.navigateToPerson(personId, watchlistRoutePattern)
            },
            mediaClick = { mediaId, mediaType, detailsOrigin, _, _ ->
                navController.navigateToMediaDetail(
                    mediaId,
                    mediaType,
                    watchlistRoutePattern,
                    detailsOrigin,
                )
            }
        )

        personScreen(watchlistRoutePattern) { mediaId, mediaType ->
            navController.navigateToMediaDetail(mediaId, mediaType, watchlistRoutePattern)
        }
    }
}

internal const val watchlistRoutePattern = "watchlistScreen"

internal fun NavGraphBuilder.watchlistScreen(
    popup: () -> Unit,
    onMediaClick: (
        mediaId: Int, mediaType: String, detailsOrigin: DetailsOrigin,
    ) -> Unit,
) {
    composable(route = watchlistRoutePattern) {
        BackHandler { popup() }

        WatchlistScreen(onMediaClick)
    }
}