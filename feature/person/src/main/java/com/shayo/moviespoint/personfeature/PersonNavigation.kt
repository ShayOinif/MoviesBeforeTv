package com.shayo.moviespoint.personfeature

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

fun NavController.navigateToPerson(personId: Int, route: String) {
    navigate("$route/$personRoutePattern/$personId")
}

internal const val personRoutePattern = "personScreen"

fun NavGraphBuilder.personScreen(
    route: String,
    onMediaClicked: (mediaId: Int, mediaType: String) -> Unit,
) {
    composable(route = "$route/$personRoutePattern/{personId}",
        arguments = listOf(
            navArgument(
                name = "personId",
            ) {
                type = NavType.IntType
            },
        )
    ) {
        val personId = it.arguments?.getInt("personId") ?: throw(Exception("No valid media id")) // TODO:

        PersonScreen(personId = personId, onMediaClicked = onMediaClicked)
    }
}