package com.shayo.moviespoint.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.shayo.moviespoint.ui.MediaCard
import com.shayo.moviespoint.ui.MediaCardItem
import kotlinx.coroutines.launch

internal enum class MediaRowContentTypes {
    LOADING,
    ERROR,
    HEADER,
    CATEGORY_ROW,
}

// TODO: Change to Media category and put in components with the row of medias, get a variable to tell which media to load or something
// TODO: Reusable with the view model, less code
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    //modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val moviesPaging = homeViewModel.moviesFlow.collectAsLazyPagingItems()

    when (moviesPaging.loadState.refresh) {
        is LoadState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Error loading, retry?"
                )
                Spacer(
                    modifier = Modifier.width(8.dp)
                )
                IconButton(
                    onClick = moviesPaging::refresh,
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry loading"
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                stickyHeader(
                    key = -1,
                    contentType = MediaRowContentTypes.HEADER,
                ) {
                    Card(
                        modifier = Modifier.padding(all = 8.dp)
                    ) {
                        Text(
                            text = "Popular Movies",
                            modifier = Modifier.padding(all = 16.dp),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }
                }

                item(
                    key = "popular", //category.id,
                    contentType = MediaRowContentTypes.CATEGORY_ROW,
                ) {
                    MediaRow(
                        medias = moviesPaging,//categoriesPagingItems[category.id] ?: throw Exception("Missing category!"),
                        watchlistCallback = homeViewModel::watchlistClick,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MediaRow(
    medias: LazyPagingItems<MediaCardItem>,
    watchlistCallback: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        // TODO: Manage states somewhere else, maybe...
        val listState = rememberSaveable(saver = LazyListState.Saver) {
            LazyListState()
        }

        // Remember a CoroutineScope to be able to launch TODO: Maybe get from app state
        val coroutineScope = rememberCoroutineScope()

        LazyRow(
            modifier = modifier.fillMaxWidth(),
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            mediaRowHeader(
                append = false,
                state = medias.loadState.source.prepend,
                retryCallback = medias::retry
            )

            items(
                items = medias,
                key = { item ->
                    item.id
                },
            ) { item ->
                item?.let {
                    MediaCard(
                        item = item,
                        watchlistCallback = { watchlistCallback(item.id) }
                    )
                }
            }

            mediaRowHeader(
                append = true,
                state = medias.loadState.source.append,
                retryCallback = {
                    medias.retry()
                    coroutineScope.launch {
                        listState.animateScrollBy(56F) // TODO:
                    }
                }
            )
        }

        // Show the button if the first visible item is past
        // the first item. We use a remembered derived state to
        // minimize unnecessary compositions
        val showButton by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0
            }
        }

        AnimatedVisibility(visible = showButton) {
            Button(
                modifier = Modifier.padding(end = 16.dp),
                onClick = {
                    coroutineScope.launch {
                        // Animate scroll to the first item
                        listState.animateScrollToItem(index = 0)
                    }
                }
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    null
                )
            }
        }
    }
}

internal const val HEADER_LOADING_KEY = -1
internal const val HEADER_ERROR_KEY = -2
internal const val FOOTER_LOADING_KEY = -3
internal const val FOOTER_ERROR_KEY = -4

// TODO: Move somewhere else since it is being used in several places
internal fun LazyListScope.mediaRowHeader(
    append: Boolean,
    state: LoadState,
    retryCallback: () -> Unit,
) {
    when (state) {
        LoadState.Loading -> {
            item(
                key = if (append) FOOTER_LOADING_KEY else HEADER_LOADING_KEY,
                contentType = MediaRowContentTypes.LOADING,
            ) {
                LoadingBox()
            }
        }
        is LoadState.Error -> {
            item(
                key = if (append) FOOTER_ERROR_KEY else HEADER_ERROR_KEY,
                contentType = MediaRowContentTypes.ERROR,
            ) {
                LoadError(retryCallback = retryCallback)
            }
        }
        else -> {}
    }
}

@Composable
internal fun LoadingBox(
    modifier: Modifier = Modifier,
) {
    CircularProgressIndicator(
        modifier = modifier
            .padding(horizontal = 16.dp)
    )
}

@Composable
internal fun LoadError(
    retryCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.error_loading_text)
        )

        IconButton(onClick = { retryCallback() }) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(id = R.string.load_retry_desc)
            )
        }
    }
}