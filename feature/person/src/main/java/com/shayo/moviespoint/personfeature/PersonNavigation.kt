package com.shayo.moviespoint.personfeature

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument

const val PersonGraphRoutePattern = "person"

fun NavGraphBuilder.personGraph(
    //navController: NavController,
    onMediaClicked: (mediaId: Int, mediaType: String) -> Unit,
) {
    navigation(
        startDestination = personRoutePattern,
        route = PersonGraphRoutePattern,
    ) {
        personScreen(onMediaClicked)
    }
}

fun NavController.navigateToPerson(personId: Int,) {
    navigate("$personRoutePattern/$personId")
}

internal const val personRoutePattern = "personScreen"

internal fun NavGraphBuilder.personScreen(
    onMediaClicked: (mediaId: Int, mediaType: String) -> Unit,
) {
    composable(route = "$personRoutePattern/{personId}",
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