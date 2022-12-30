package com.shayo.moviespoint

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.shayo.moviespoint.account.AccountGraphRoutePattern
import com.shayo.moviespoint.home.HomeGraphRoutePattern
import com.shayo.moviespoint.search.SearchGraphRoutePattern
import com.shayo.moviespoint.watchlist.WatchlistGraphRoutePattern

sealed class MoviesPointScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : MoviesPointScreen(HomeGraphRoutePattern, "Home", Icons.Default.Home)
    object Search : MoviesPointScreen(SearchGraphRoutePattern, "Search", Icons.Default.Search)
    object Watchlist :
        MoviesPointScreen(WatchlistGraphRoutePattern, "Watchlist", Icons.Default.List)

    object Account :
        MoviesPointScreen(AccountGraphRoutePattern, "Account", Icons.Default.AccountCircle)
}

val screens = listOf(
    MoviesPointScreen.Home,
    MoviesPointScreen.Search,
    MoviesPointScreen.Watchlist,
    MoviesPointScreen.Account,
)