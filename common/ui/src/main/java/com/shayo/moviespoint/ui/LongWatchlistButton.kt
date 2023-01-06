package com.shayo.moviespoint.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun LongWatchlistButton(
    inWatchlist: Boolean?,
    modifier: Modifier = Modifier,
    watchlistCallback: () -> Unit,
) {
    TextButton(onClick = watchlistCallback, modifier = modifier) {
        inWatchlist?.let {
            WatchlistIcon(inWatchlist = inWatchlist)

            Spacer(
                modifier = Modifier.size(8.dp)
            )

            Text(
                text = if (inWatchlist) {
                    stringResource(id = R.string.remove_from_watchlist)
                } else {
                    stringResource(id = R.string.add_to_watchlist)
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (inWatchlist) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            )
        } ?: CircularProgressIndicator()
    }
}