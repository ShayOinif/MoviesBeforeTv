package com.shayo.moviespoint.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WatchlistIcon(
    inWatchlist: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = if (inWatchlist) {
            Icons.Filled.RemoveCircle
        } else {
            Icons.Filled.AddCircle
        },
        contentDescription = null,
        modifier = modifier,
        tint = if (inWatchlist) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        }
    )
}