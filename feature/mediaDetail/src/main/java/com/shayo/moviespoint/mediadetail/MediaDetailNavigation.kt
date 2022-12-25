package com.shayo.moviespoint.mediadetail

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument

const val MediaDetailGraphRoutePattern = "mediaDetail"

fun NavGraphBuilder.mediaDetailGraph(
    //navController: NavController,
) {
    navigation(
        startDestination = mediaDetailRoutePattern,
        route = MediaDetailGraphRoutePattern,
    ) {
        mediaDetailScreen()
    }
}

fun NavController.navigateToMediaDetail(mediaId: Int, mediaType: String,) {
    navigate("$mediaDetailRoutePattern/$mediaId/$mediaType")
}

internal const val mediaDetailRoutePattern = "mediaDetailScreen"

internal fun NavGraphBuilder.mediaDetailScreen() {
    composable(route = "$mediaDetailRoutePattern/{mediaId}/{mediaType}",
        arguments = listOf(
            navArgument(
                name = "mediaId",
            ) {
                type = NavType.IntType
            },
            navArgument(
                name = "mediaType",
            ) {
                type = NavType.StringType
            },
        )) {
        val mediaId = it.arguments?.getInt("mediaId") ?: throw(Exception("No valid media id")) // TODO:
        val mediaType = it.arguments?.getString("mediaType") ?: throw(Exception("No valid media type")) // TODO:

        MediaDetailScreen(
            mediaId,
            mediaType,
        )
    }
}