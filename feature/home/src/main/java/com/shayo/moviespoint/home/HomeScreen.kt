package com.shayo.moviespoint.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.shayo.moviespoint.ui.MediaCard
import com.shayo.moviespoint.ui.MediaCardItem
import com.shayo.moviespoint.viewmodels.home.HomeViewModel
import kotlinx.coroutines.launch

internal enum class MediaRowContentTypes {
    LOADING,
    ERROR,
    HEADER,
    CATEGORY_ROW,
}

internal data class HomeItem(
    val id: Int,
    val type: String,
    val mediaCardItem: MediaCardItem,
)

// TODO: Change to Media category and put in components with the row of medias, get a variable to tell which media to load or something
// TODO: Reusable with the view model, less code
// TODO: Handle not remembering state in the column, the row is fine

@OptIn(ExperimentalFoundationApi::class, ExperimentalLifecycleComposeApi::class)
@Composable
internal fun HomeScreen(
    //modifier: Modifier = Modifier,
    onMediaClick: (mediaId: Int, mediaType: String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = true) {
        homeViewModel.setup(
            withGenres = false,
            HomeItem::class,
        ) { pagedMovie, _ ->
            with(pagedMovie.movie) {
                HomeItem(
                    id, type,
                    MediaCardItem(
                        posterPath,
                        title,
                        "%.1f".format(voteAverage),
                        releaseDate,
                        inWatchlist = isFavorite,
                    )
                )
            }
        }
    }

    val categories by homeViewModel.categoriesFlows.collectAsStateWithLifecycle()

    categories?.let { currentCategories ->
        val categoriesListState = rememberSaveable(saver = LazyListState.Saver) {
            LazyListState()
        }

        val categoriesPagingItems = currentCategories.associate { homeCategory ->
            Pair(homeCategory.nameRes, homeCategory.flow.collectAsLazyPagingItems())
        }

        val refreshState by remember {
            derivedStateOf {
                categoriesPagingItems.map { (_, pagingItems) ->
                    pagingItems.loadState.refresh
                }
            }
        }

        when {
            refreshState.contains(LoadState.Loading) -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            refreshState.any { categoryRefreshState ->
                categoryRefreshState is LoadState.Error
            } -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Errors:\n${
                            categoriesPagingItems.filter { (_, pagingItems) ->
                                pagingItems.loadState.refresh is LoadState.Error
                            }.map { (_, pagingItems) ->
                                (pagingItems.loadState.refresh as LoadState.Error).error.message ?: ""
                            }.toSet().joinToString(".\n") { it }
                        }.\nRetry?",
                    )
                    Spacer(
                        modifier = Modifier.size(16.dp)
                    )
                    IconButton(
                        onClick = {
                            categoriesPagingItems.filter { (_, pagingItems) ->
                                pagingItems.loadState.refresh is LoadState.Error
                            }.forEach { (_, pagingItems) ->
                                pagingItems.refresh()
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        colors = IconButtonDefaults.filledIconButtonColors(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.load_retry_desc)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    state = categoriesListState,
                    contentPadding = PaddingValues(
                        bottom = 8.dp
                    )
                ) {
                    for (category in currentCategories) {
                        stickyHeader(
                            key = -category.nameRes,
                            contentType = MediaRowContentTypes.HEADER,
                        ) {
                            Card(
                                modifier = Modifier.padding(all = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = category.nameRes),
                                    modifier = Modifier
                                        .padding(
                                            all = 16.dp,
                                        ),
                                    style = MaterialTheme.typography.headlineLarge,
                                )
                            }
                        }

                        item(
                            key = category.nameRes,
                            contentType = MediaRowContentTypes.CATEGORY_ROW,
                        ) {
                            MediaRow(
                                medias = categoriesPagingItems[category.nameRes] as? LazyPagingItems<HomeItem>
                                    ?: throw Exception("Missing category!"), // TODO: Handle nullness and uncheked cast
                                watchlistCallback = homeViewModel::watchlistClick,
                                onMediaClick = onMediaClick
                            )
                        }
                    }
                }
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun MediaRow(
    medias: LazyPagingItems<HomeItem>,
    watchlistCallback: (id: Int, type: String) -> Unit,
    onMediaClick: (mediaId: Int, mediaType: String) -> Unit,
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
                state = medias.loadState.prepend,
                retryCallback = medias::retry
            )

            items(
                items = medias,
                key = { item ->
                    item.id
                },
            ) { homeItem ->
                homeItem?.let {
                        MediaCard(
                            item = homeItem.mediaCardItem,
                            watchlistCallback = { watchlistCallback(homeItem.id, homeItem.type) },
                            onClickCallback = { onMediaClick(homeItem.id, homeItem.type) }
                        )
                }
            }

            mediaRowHeader(
                append = true,
                state = medias.loadState.append,
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
                LoadError(
                    state.error.message,
                    retryCallback = retryCallback,
                )
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
    message: String?,
    retryCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(220.dp)
            .fillMaxHeight()
            .padding(end = 80.dp),
    ) {
        Text(
            text = message?.let { "Error: $it. Retry?" }
                ?: stringResource(id = R.string.error_loading_text),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.size(8.dp))

        IconButton(
            onClick = { retryCallback() },
            modifier = Modifier
                .padding(start = 40.dp)
                .size(56.dp)
                .clip(CircleShape),
            colors = IconButtonDefaults.filledIconButtonColors(),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(id = R.string.load_retry_desc)
            )
        }
    }
}