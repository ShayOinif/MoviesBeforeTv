package com.shayo.moviespoint.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.shayo.moviespoint.mediadetail.mediaDetailScreen
import com.shayo.moviespoint.mediadetail.navigateToMediaDetail
import com.shayo.moviespoint.personfeature.navigateToPerson
import com.shayo.moviespoint.personfeature.personScreen
import com.shayo.moviespoint.ui.DetailsOrigin

const val HomeGraphRoutePattern = "home"

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
) {


    navigation(
        startDestination = homeRoutePattern,
        route = HomeGraphRoutePattern,
    ) {
        homeScreen(
            onMediaClick = { mediaId, mediaType, detailsOrigin, queryOrCategory, position ->
                navController.navigateToMediaDetail(mediaId, mediaType, homeRoutePattern, detailsOrigin, queryOrCategory, position)
            }
        )

        mediaDetailScreen(homeRoutePattern,  { personId ->
            navController.navigateToPerson(personId, homeRoutePattern)
        }
        ) { mediaId, mediaType, detailsOrigin, queryOrCategory, position ->
            navController.navigateToMediaDetail(mediaId, mediaType, homeRoutePattern, detailsOrigin, queryOrCategory, position)
        }

        personScreen(homeRoutePattern) { mediaId, mediaType ->
            navController.navigateToMediaDetail(mediaId, mediaType, homeRoutePattern)
        }
    }
}

internal const val homeRoutePattern = "homeScreen"

internal fun NavGraphBuilder.homeScreen(
    onMediaClick: (mediaId: Int, mediaType: String, detailsOrigin: DetailsOrigin,
                   queryOrCategory: String,
                   position: Int) -> Unit,
) {
    composable(route = homeRoutePattern) {
        HomeScreen(onMediaClick)
    }
}