package com.shayo.moviespoint.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun MediaCard(
    item: MediaCardItem,
    watchlistCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(154.dp) // TODO:
    ) {

        item.posterPath?.let {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current).tag("Coil request for image") // TODO
                    .data("https://image.tmdb.org/t/p/w154${item.posterPath}") // TODO: Get base url from somewhere else
                    .crossfade(true)
                    .build(),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Icon(
                        Icons.Default.BrokenImage,
                        null,
                        modifier = Modifier.fillMaxSize()
                    )
                },
                contentDescription = null,
                modifier = Modifier
                    .width(154.dp) // TODO: Maybe move to a const
                    .aspectRatio(2 / 3F) // TODO: Maybe move to a const
            )
        } ?: run {
            Image(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                modifier = Modifier
                    .width(154.dp) // TODO: Maybe move to a const
                    .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                    .padding(horizontal = 16.dp)
            )
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(8.dp),
        )

        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = item.voteAverage, style = MaterialTheme.typography.labelLarge)
            }

            if (isLandscape) {
                IconButton(onClick = watchlistCallback) {
                    WatchlistIcon(inWatchlist = item.inWatchlist)
                }
            }
        }

        // TODO: Maybe create another clickable surface
        if (!isLandscape) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { watchlistCallback() },
            ) {
                WatchlistIcon(inWatchlist = item.inWatchlist)

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    text = if (item.inWatchlist) {
                        stringResource(id = R.string.remove_from_watchlist)
                    } else {
                        stringResource(id = R.string.add_to_watchlist)
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun WatchlistIcon(
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
            LocalContentColor.current
        }
    )
}