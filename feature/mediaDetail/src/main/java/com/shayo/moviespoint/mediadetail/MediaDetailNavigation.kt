package com.shayo.moviespoint.mediadetail

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shayo.moviespoint.ui.DetailsOrigin

fun NavController.navigateToMediaDetail(mediaId: Int, mediaType: String, route: String,
                                        detailsOrigin: DetailsOrigin? = DetailsOrigin.NONE,
                                        queryOrCategory: String? = null,
                                        position: Int = -1,
                                        ) {
    navigate("$route/$mediaDetailRoutePattern/$mediaId/$mediaType/$detailsOrigin/$queryOrCategory/$position")
}

internal const val mediaDetailRoutePattern = "mediaDetailScreen"

fun NavGraphBuilder.mediaDetailScreen(
    route: String,
    personClick: (personId: Int) -> Unit,
    mediaClick: (mediaId: Int, mediaType: String,
                 detailsOrigin: DetailsOrigin?,
                 queryOrCategory: String?,
                 position: Int,
    ) -> Unit = {_, _, _, _, _ -> },
) {
    composable(route = "$route/$mediaDetailRoutePattern/{mediaId}/{mediaType}/{detailsOrigin}/{queryOrCategory}/{position}",
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
            navArgument(
                name = "detailsOrigin",
            ) {
                type = NavType.EnumType(DetailsOrigin::class.java)
            },
            navArgument(
                name = "queryOrCategory",
            ) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(
                name = "position",
            ) {
                type = NavType.IntType
            },
        )) {
        val mediaId = it.arguments?.getInt("mediaId") ?: throw(Exception("No valid media id")) // TODO:
        val mediaType = it.arguments?.getString("mediaType") ?: throw(Exception("No valid media type")) // TODO:
        val detailsOrigin = it.arguments?.get("detailsOrigin") as DetailsOrigin
        val queryOrCategory = it.arguments?.getString("queryOrCategory")
        val position = it.arguments?.getInt("position")

        MediaDetailScreen(
            mediaId,
            mediaType,
            personClick,
            onMediaClick = {
                    mediaId: Int, mediaType: String,
                    detailsOrigin: DetailsOrigin?,
                    queryOrCategory: String?,
                    position: Int ->
                mediaClick(mediaId, mediaType, detailsOrigin, queryOrCategory, position)
            },
            detailsOrigin,
            queryOrCategory,
            position,
        )
    }
}