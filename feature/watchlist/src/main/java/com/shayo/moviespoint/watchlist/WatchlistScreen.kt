package com.shayo.moviespoint.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shayo.moviespoint.ui.MediaCard

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
internal fun WatchlistScreen(
    //modifier: Modifier = Modifier,
    onMediaClick: (mediaId: Int, mediaType: String) -> Unit,
    watchlistViewModel: WatchlistViewModel = hiltViewModel(),
) {
    val watchlistItems by watchlistViewModel.watchlistFlow.collectAsStateWithLifecycle()

    if (watchlistItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.List,
                    null,
                    modifier = Modifier.size(80.dp)
                )
                Text(
                    text = "Your Watchlist Is Empty",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(154.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = watchlistItems,
                key = { item ->
                    item.id
                },
            ) { item ->
                MediaCard(
                    item = item.mediaCardItem,
                    watchlistCallback = { watchlistViewModel.watchlistClick(item.id, item.type) },
                    onClickCallback = {
                        onMediaClick(item.id, item.type)
                    }
                )
            }
        }
    }
}