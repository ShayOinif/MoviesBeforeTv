package com.shayo.moviespoint.search

import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.shayo.moviespoint.mediadetail.mediaDetailScreen
import com.shayo.moviespoint.mediadetail.navigateToMediaDetail
import com.shayo.moviespoint.personfeature.navigateToPerson
import com.shayo.moviespoint.personfeature.personScreen

const val SearchGraphRoutePattern = "search"
internal const val searchRoutePattern = "searchScreen"

fun NavGraphBuilder.searchGraph(
    popup: () -> Unit,
    navController: NavController,
) {
    navigation(
        startDestination = searchRoutePattern,
        route = SearchGraphRoutePattern,
    ) {
        searchScreen(
            popup = popup,
            onMediaClicked = { mediaId, mediaType ->
                navController.navigateToMediaDetail(mediaId, mediaType, searchRoutePattern)
            },
            onPersonClicked = { personId ->
                navController.navigateToPerson(personId, searchRoutePattern)
            }
        )

        mediaDetailScreen(searchRoutePattern, { personId ->
            navController.navigateToPerson(personId, searchRoutePattern)
        }
        )

        personScreen(searchRoutePattern) { mediaId, mediaType ->
            navController.navigateToMediaDetail(mediaId, mediaType, searchRoutePattern)
        }
    }
}

internal fun NavGraphBuilder.searchScreen(
    popup: () -> Unit,
    onMediaClicked: (mediaId: Int, mediaType: String) -> Unit,
    onPersonClicked: (personId: Int) -> Unit,
) {
    composable(route = searchRoutePattern) {
        BackHandler { popup() }

        SearchScreen(onMediaClicked, onPersonClicked)
    }
}