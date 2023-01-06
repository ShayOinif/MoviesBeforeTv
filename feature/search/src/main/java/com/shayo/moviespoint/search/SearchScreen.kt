package com.shayo.moviespoint.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.shayo.movies.PagedItem
import com.shayo.moviespoint.ui.LongWatchlistButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
internal fun SearchScreen(
    //modifier: Modifier = Modifier,
    onMediaClicked: (mediaId: Int, mediaType: String) -> Unit,
    onPersonClicked: (personId: Int) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val appBarState = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val scaffoldModifier = remember { Modifier.nestedScroll(appBarState.nestedScrollConnection) }

    val query by searchViewModel.query.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }

    val searchListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val historyListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(true) {
        searchViewModel.query.drop(1).collectLatest {
            if (searchListState.firstVisibleItemIndex != 0)
                searchListState.animateScrollToItem(0)

            if (historyListState.firstVisibleItemIndex != 0)
                historyListState.animateScrollToItem(0)
        }
    }

    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler(
        enabled = query.isNotEmpty() || appBarState.state.collapsedFraction != 0.0F
    ) {
        if (appBarState.state.collapsedFraction != 0.0F) {
            scope.launch {

                if (searchListState.firstVisibleItemIndex != 0)
                    searchListState.animateScrollToItem(0)

                if (historyListState.firstVisibleItemIndex != 0)
                    historyListState.animateScrollToItem(0)

                /*appBarState.nestedScrollConnection.onPreScroll(
                    Offset.Infinite,
                    NestedScrollSource.Fling
                )
                appBarState.nestedScrollConnection.onPostScroll(
                    Offset.Infinite,
                    Offset.Zero,
                    NestedScrollSource.Fling
                )
                appBarState.nestedScrollConnection.onPostFling(
                    Velocity(0F, Offset.Infinite.y),
                    Velocity.Zero
                )*/

                appBarState.state.heightOffset = 0F

                focusRequester.requestFocus()

                keyboardController?.show()
            }
        } else {
            // TODO: Think if we want to show the keyboard twice

            searchViewModel.onQueryTextChange("")

            focusRequester.requestFocus()

            keyboardController?.show()
        }
    }

    Scaffold(
        modifier = scaffoldModifier,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = searchViewModel::onQueryTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .focusRequester(focusRequester),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                            )
                        },
                        label = {
                            Text(
                                text = "Search", // TODO: Move to string res
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Type To Search", // TODO: Move to string res
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    searchViewModel.onQueryTextChange("")
                                    focusRequester.requestFocus()
                                },
                            ) {
                                Icon(Icons.Default.Delete, "Clear Search")
                            }
                        },
                        enabled = true,
                        singleLine = true,
                        maxLines = 1, // TODO: Make is a search keyboard,
                    )
                },
                scrollBehavior = appBarState,
            )
        }
    ) { paddingValues ->
        val history by searchViewModel.historyFlow.collectAsStateWithLifecycle()

        if (query.isEmpty() && history?.isEmpty() == true) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Search For Movies, TV Shows and Persons",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else if (query.isEmpty() && history?.isEmpty() == false) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                state = historyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(
                    items = history!!,
                    key = { it }
                ) { query ->
                    ListItem(
                        headlineText = {
                            Text(
                                text = query,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        },
                        modifier = Modifier.clickable {
                            searchViewModel.onQueryTextChange(query)

                            searchViewModel.onResultClick(query)
                        }
                    )

                    Divider()
                }

                item {
                    TextButton(onClick = { searchViewModel.deleteHistory() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_history),
                        )
                    }

                    Text(text = stringResource(R.string.delete_history))
                }
            }
        } else {
            SearchResults(
                results = searchViewModel.searchFlow.collectAsLazyPagingItems(),
                onMediaClicked = { mediaId, mediaType, title ->
                    searchViewModel.onResultClick(title)
                    onMediaClicked(mediaId, mediaType)
                },
                onPersonClicked = { personId, personName ->
                    searchViewModel.onResultClick(personName)
                    onPersonClicked(personId)
                },
                onWatchlistClick = searchViewModel::watchlistClick,
                searchListState = searchListState,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchResults(
    results: LazyPagingItems<PagedItem>,
    onMediaClicked: (mediaId: Int, mediaType: String, title: String) -> Unit,
    onPersonClicked: (personId: Int, personName: String) -> Unit,
    onWatchlistClick: (id: Int, type: String) -> Unit,
    searchListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    when {
        results.loadState.refresh is LoadState.Loading -> { // TODO: Better handle everything, maybe make a better use of hte media header below
            LinearProgressIndicator(
                modifier = modifier.fillMaxWidth()
            )
        }
        results.loadState.refresh is LoadState.Error -> {
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Error: ${(results.loadState.refresh as LoadState.Error).error.message}, Retry?",
                )

                Spacer(modifier = Modifier.size(8.dp))

                IconButton(
                    onClick = { results.refresh() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.filledIconButtonColors(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(id = R.string.load_retry_desc),
                    )
                }
            }
        }
        results.itemCount == 0 -> {
            Box(
                modifier = modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No Results",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(),
                state = searchListState
            ) {

                mediaRowHeader(
                    append = false,
                    state = results.loadState.source.prepend,
                    retryCallback = results::retry
                )

                items(
                    items = results,
                    key = { pagedItem ->
                        when (pagedItem) {
                            is PagedItem.PagedCredit -> "${pagedItem.credit.id}c"
                            is PagedItem.PagedMovie -> "${pagedItem.movie.id}m${pagedItem.position}"
                        }
                    }
                ) { pagedItem ->
                    pagedItem?.let { item ->
                        ListItem(
                            modifier = Modifier.clickable {
                                when (item) {
                                    is PagedItem.PagedCredit -> {
                                        onPersonClicked(item.credit.id, item.credit.name)
                                    }
                                    is PagedItem.PagedMovie -> onMediaClicked(
                                        item.movie.id,
                                        item.movie.type,
                                        item.movie.title,
                                    )
                                }
                            },
                            headlineText = {
                                Text(
                                    text = when (item) {
                                        is PagedItem.PagedCredit -> item.credit.name
                                        is PagedItem.PagedMovie -> item.movie.title
                                    },
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                )
                            },
                            overlineText = {
                                Text(
                                    when (item) {
                                        is PagedItem.PagedCredit -> stringResource(id = R.string.person)
                                        is PagedItem.PagedMovie -> stringResource(
                                            id = if (item.movie.type == "movie")
                                                R.string.movie
                                            else
                                                R.string.show
                                        )
                                    }
                                )
                            },
                            supportingText = {
                                Text(
                                    when (item) {
                                        is PagedItem.PagedCredit -> item.credit.knownFor.take(4)
                                            .joinToString(", ") { it.title }
                                        is PagedItem.PagedMovie -> "Rating: ${"%.1f".format(item.movie.voteAverage)}/10\n${
                                            stringResource(
                                                id = R.string.release_date,
                                                item.movie.releaseDate ?: stringResource(id = R.string.not_available),
                                            )
                                        }"
                                    }
                                )

                                if (item is PagedItem.PagedMovie) {
                                    LongWatchlistButton(inWatchlist = item.movie.isFavorite) {
                                        onWatchlistClick(item.movie.id, item.movie.type)
                                    }
                                }
                            },
                            leadingContent = {
                                // TODO: This also used in the cards above, extract to a shared component

                                val posterPath = when (item) {
                                    is PagedItem.PagedCredit -> item.credit.profilePath?.let { "https://image.tmdb.org/t/p/original${item.credit.profilePath}" }
                                    is PagedItem.PagedMovie -> item.movie.posterPath?.let { "https://image.tmdb.org/t/p/w154${item.movie.posterPath}" }
                                }

                                posterPath?.let {
                                    var retryHash by remember { mutableStateOf(0) }

                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(posterPath)
                                            .setParameter(
                                                "retry_hash",
                                                retryHash,
                                                memoryCacheKey = null
                                            )
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
                                            .widthIn(max = 92.dp) // TODO: Maybe move to a const and get the smaller image from config repo
                                            .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                                    )
                                } ?: Image(
                                    imageVector = when (item) {
                                        is PagedItem.PagedCredit -> Icons.Default.Person
                                        is PagedItem.PagedMovie -> if (item.movie.type == "movie")
                                            Icons.Default.LocalMovies
                                        else
                                            Icons.Default.Tv
                                    },
                                    contentDescription = null,
                                    modifier = Modifier
                                        .widthIn(max = 92.dp) // TODO: Maybe move to a const
                                        .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                                        .padding(horizontal = 16.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                                )
                            }
                        )
                    } ?: ListItem(
                        headlineText = {
                            Text("Loading")
                        },
                        overlineText = {
                            Text("Loading")
                        },
                        supportingText = {
                            Text("Loading")
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 92.dp) // TODO: Maybe move to a const and get the smaller image from config repo
                                    .aspectRatio(2 / 3F), // TODO: Maybe move to a const,
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    )

                    Divider()
                }

                mediaRowHeader(
                    append = true,
                    state = results.loadState.source.append,
                    retryCallback = results::retry
                )

                // TODO: Create key and content type to this item and put more effort in ui
                if (results.loadState.source.append.endOfPaginationReached) {
                    item {
                        ListItem(
                            headlineText = {
                                Text("End of results")
                            },
                        )
                    }
                }
            }
        }
    }
}

// TODO: Use the shared one, right now it is used in home screen as well
internal const val HEADER_LOADING_KEY = -1
internal const val HEADER_ERROR_KEY = -2
internal const val FOOTER_LOADING_KEY = -3
internal const val FOOTER_ERROR_KEY = -4

internal enum class MediaRowContentTypes {
    LOADING,
    ERROR,
}

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
                    message = state.error.message,
                    retryCallback = retryCallback
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
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = modifier
                .padding(16.dp)
        )
    }
}

@Composable
internal fun LoadError(
    message: String?,
    retryCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error${message?.let { ": $it" } ?: " Loading"}, Retry?"
        )

        Spacer(modifier = Modifier.size(8.dp))

        IconButton(
            onClick = retryCallback,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            colors = IconButtonDefaults.filledIconButtonColors(),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(id = R.string.load_retry_desc),
            )
        }
    }
}