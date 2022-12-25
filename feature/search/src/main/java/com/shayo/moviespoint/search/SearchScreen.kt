package com.shayo.moviespoint.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
internal fun SearchScreen(
    //modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val appBarState = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scaffoldModifier = Modifier.nestedScroll(appBarState.nestedScrollConnection)

    val query by searchViewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        modifier = scaffoldModifier,
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = searchViewModel::onQueryTextChange,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
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
                                text = "Search", // TODO: Move to string res
                            )
                        },
                        enabled = true,
                        singleLine = true,
                        maxLines =  1, // TODO: Make is a search keyboard,
                    )
                },
                scrollBehavior = appBarState,
            )
        }
    ) { paddingValues ->
        if (query.isEmpty()) {
            Box(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Type to search",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        } else {
            SearchResults(
                results = searchViewModel.searchFlow.collectAsLazyPagingItems(),
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchResults(
    results: LazyPagingItems<PagedItem>,
    modifier: Modifier = Modifier,
) {
    when (results.loadState.refresh) {
        is LoadState.Loading -> { // TODO: Better handle everything, maybe make a better use of hte media header below
            LinearProgressIndicator(
                modifier = modifier.fillMaxWidth()
            )
        }
        is LoadState.Error -> {
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error loading"//stringResource(id = R.string.error_loading_text)
                )

                IconButton(onClick = { results.refresh() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null//stringResource(id = R.string.load_retry_desc)
                    )
                }
            }
        }
        else -> {
            val searchListState = rememberSaveable(saver = LazyListState.Saver) {
                LazyListState()
            }

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
                            headlineText = {
                                Text(
                                    when (item) {
                                        is PagedItem.PagedCredit -> item.credit.name
                                        is PagedItem.PagedMovie -> item.movie.title
                                    }
                                )
                            },
                            overlineText = {
                                Text(
                                    when (item) {
                                        is PagedItem.PagedCredit -> item.credit.description
                                        is PagedItem.PagedMovie -> "Release date: ${item.movie.releaseDate ?: "Not available"}"
                                    }
                                )
                            },
                            supportingText = {

                                Text(
                                    when (item) {
                                        is PagedItem.PagedCredit -> item.credit.knownFor.take(4).joinToString(", ") { it.title }
                                        is PagedItem.PagedMovie -> "Average score: ${item.movie.voteAverage}"
                                    }
                                )
                            },
                            leadingContent = {
                                // TODO: This also used in the cards above, extract to a shared component

                                val posterPath = when (item) {
                                    is PagedItem.PagedCredit -> item.credit.profilePath?.let {"https://image.tmdb.org/t/p/original${item.credit.profilePath}"}
                                    is PagedItem.PagedMovie -> item.movie.posterPath?.let {"https://image.tmdb.org/t/p/w154${item.movie.posterPath}" }
                                }

                                if (posterPath != null) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(posterPath)
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
                                                Icons.Filled.BrokenImage,
                                                null,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .widthIn(max = 92.dp) // TODO: Maybe move to a const and get the smaller image from config repo
                                            .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                                    )
                                } else {
                                    Image(
                                        imageVector = Icons.Outlined.CloudOff,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .widthIn(max = 92.dp) // TODO: Maybe move to a const
                                            .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                                            .padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        )
                    } ?: run {
                        ListItem(
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
                    }

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
    HEADER,
    CATEGORY_ROW,
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
            text = "Error loading"//stringResource(id = R.string.error_loading_text)
        )

        IconButton(onClick = { retryCallback() }) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null//stringResource(id = R.string.load_retry_desc)
            )
        }
    }
}