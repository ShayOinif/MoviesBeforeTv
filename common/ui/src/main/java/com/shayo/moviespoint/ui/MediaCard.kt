package com.shayo.moviespoint.ui

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun MediaCard(
    item: MediaCardItem?,
    watchlistCallback: () -> Unit,
    modifier: Modifier = Modifier,
    onClickCallback: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .width(154.dp) // TODO:
            .clickable {
                onClickCallback()
            }
    ) {

        item?.posterPath?.let {

            var retryHash by remember { mutableStateOf(0) }

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .tag("Coil request for image") // TODO
                    .data("https://image.tmdb.org/t/p/w154${item.posterPath}") // TODO: Get base url from somewhere else
                    .crossfade(true)
                    .setParameter("retry_hash", retryHash, memoryCacheKey = null)
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = { retryHash++ }) {
                            Icon(
                                Icons.Filled.Refresh,
                                null,
                            )
                        }
                    }
                },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth() // TODO: Maybe move to a const
                    .aspectRatio(2 / 3F) // TODO: Maybe move to a const
            )
        } ?: run {
            item?.let {
                Image(
                    imageVector = if (item.type == "movie") Icons.Default.LocalMovies
                    else Icons.Default.Tv,
                    contentDescription = null,
                    modifier = Modifier
                        .width(154.dp) // TODO: Maybe move to a const
                        .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                        .padding(horizontal = 16.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            } ?: Box(
                modifier = Modifier
                    .width(154.dp) // TODO: Maybe move to a const
                    .aspectRatio(2 / 3F), // TODO: Maybe move to a const,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        Text(
            text = item?.title ?: "",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(8.dp),
        )

        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = item?.voteAverage ?: "",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                item?.let {
                    Icon(
                        imageVector = if (item.type == "movie")
                            Icons.Default.LocalMovies
                        else
                            Icons.Default.Tv,
                        contentDescription = if (item.type == "movie")
                            "Movie"
                        else
                            "TV Show",
                    )
                }

                if (isLandscape) {
                    item?.inWatchlist?.let {
                        IconButton(onClick = watchlistCallback) {
                            WatchlistIcon(inWatchlist = item.inWatchlist)
                        }
                    } ?: run { Log.d("MyTAg", "Loading")
                        CircularProgressIndicator() }
                }
            }
        }

        // TODO: Maybe create another clickable surface
        if (!isLandscape) {
            LongWatchlistButton(
                inWatchlist = item?.inWatchlist,
                watchlistCallback = watchlistCallback
            )
        }
    }
}