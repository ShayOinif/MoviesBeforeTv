package com.shayo.moviespoint.mediadetail

import android.content.Context
import android.content.pm.ActivityInfo
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.shayo.moviespoint.ui.DetailsOrigin
import com.shayo.moviespoint.ui.LongWatchlistButton
import com.shayo.moviespoint.ui.MediaCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

private const val YOUTUBE_KEY = "1"

@Suppress("DEPRECATION")
@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun MediaDetailScreen(
    mediaId: Int,
    mediaType: String,
    personClick: (personId: Int) -> Unit,
    onMediaClick: (
        mediaId: Int, mediaType: String,
        detailsOrigin: DetailsOrigin?,
        queryOrCategory: String?,
        position: Int,
    ) -> Unit,
    detailsOrigin: DetailsOrigin,
    queryOrCategory: String? = null,
    position: Int? = null,
    mediaDetailViewModel: MediaDetailViewModel = hiltViewModel(),
) {
    var isFullScreen by rememberSaveable { mutableStateOf(false) }

    BackHandler(isFullScreen) {
        mediaDetailViewModel.currentPlayer?.setFullscreen(false)
        isFullScreen = false
    }

    LaunchedEffect(key1 = true) {
        mediaDetailViewModel.setMedia(mediaId, mediaType, detailsOrigin, queryOrCategory, position)
    }

    val mediaDetailUiState by mediaDetailViewModel.detailsFlow.collectAsStateWithLifecycle()

    // TODO: Make favorites paged so we can handle them the same
    val moreItems = mediaDetailUiState?.moreFlow?.collectAsLazyPagingItems()

    val favorites = mediaDetailUiState?.favoritesFlow?.collectAsStateWithLifecycle(emptyList())

    val localContext = LocalContext.current

    val scope = rememberCoroutineScope()

    val span = remember { GridItemSpan(Int.MAX_VALUE) }

    mediaDetailUiState?.media?.let { currentMedia ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(154.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { span }) {
                if (
                    GoogleApiAvailability.getInstance()
                        .isGooglePlayServicesAvailable(LocalContext.current) == ConnectionResult.SUCCESS
                ) {
                    if (mediaDetailUiState?.videoIds?.isNotEmpty() == true) {
                        var player: YouTubePlayer? = null
                        var youtubeView: YouTubePlayerFragment? = null
                        var currContext: Context? = null
                        val uuid = UUID.randomUUID()

                        val youTubeInitializer = remember {
                            object : YouTubePlayer.OnInitializedListener {
                                override fun onInitializationSuccess(
                                    p0: YouTubePlayer.Provider?,
                                    p1: YouTubePlayer?,
                                    p2: Boolean
                                ) {
                                    player = p1
                                    mediaDetailViewModel.currentPlayer = p1

                                    p1?.setPlaybackEventListener(object :
                                        YouTubePlayer.PlaybackEventListener {
                                        override fun onPlaying() {
                                            mediaDetailViewModel.playing = true
                                        }

                                        override fun onPaused() {
                                            mediaDetailViewModel.playing = false
                                        }

                                        override fun onStopped() {
                                            mediaDetailViewModel.playing = false
                                            mediaDetailViewModel.currentDur = null
                                        }

                                        override fun onBuffering(p0: Boolean) {
                                        }

                                        override fun onSeekTo(p0: Int) {
                                        }
                                    })

                                    p1?.setPlaylistEventListener(object :
                                        YouTubePlayer.PlaylistEventListener {
                                        override fun onPrevious() {
                                            mediaDetailViewModel.currentVideo--

                                            if (mediaDetailViewModel.playing)
                                                player?.loadVideos(
                                                    mediaDetailUiState?.videoIds,
                                                    mediaDetailViewModel.currentVideo,
                                                    0
                                                )
                                            else
                                                player?.cueVideos(
                                                    mediaDetailUiState?.videoIds,
                                                    mediaDetailViewModel.currentVideo,
                                                    0
                                                )
                                        }

                                        override fun onNext() {
                                            mediaDetailViewModel.currentVideo++

                                            if (mediaDetailViewModel.playing)
                                                player?.loadVideos(
                                                    mediaDetailUiState?.videoIds,
                                                    mediaDetailViewModel.currentVideo,
                                                    0
                                                )
                                            else
                                                player?.cueVideos(
                                                    mediaDetailUiState?.videoIds,
                                                    mediaDetailViewModel.currentVideo,
                                                    0
                                                )
                                        }

                                        override fun onPlaylistEnded() {
                                        }
                                    }

                                    )

                                    p1?.setOnFullscreenListener {
                                        if (!it)
                                            (localContext as ComponentActivity).requestedOrientation =
                                                ActivityInfo.SCREEN_ORIENTATION_SENSOR

                                        isFullScreen = it
                                    }

                                    mediaDetailViewModel.currentDur?.let { currentDur ->
                                        if (mediaDetailViewModel.playing)
                                            p1?.loadVideos(
                                                mediaDetailUiState?.videoIds,
                                                mediaDetailViewModel.currentVideo,
                                                currentDur
                                            )
                                        else
                                            p1?.cueVideos(
                                                mediaDetailUiState?.videoIds,
                                                mediaDetailViewModel.currentVideo,
                                                currentDur
                                            )
                                    } ?: p1?.cueVideos(mediaDetailUiState?.videoIds)
                                }

                                override fun onInitializationFailure(
                                    p0: YouTubePlayer.Provider?,
                                    p1: YouTubeInitializationResult?
                                ) {
                                    // TODO:
                                }
                            }
                        }

                        // TODO: The button is for when there is an early inflation problem, fix another way
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    // TODO: Move some logic to functions for reusability
                                    player?.release()
                                    youtubeView?.onDestroy()

                                    youtubeView = YouTubePlayerFragment()

                                    (currContext as ComponentActivity).fragmentManager
                                        .beginTransaction()
                                        .replace(
                                            R.id.player_frame,
                                            youtubeView,
                                            null
                                        )
                                        .commit()


                                    youtubeView?.initialize(YOUTUBE_KEY, youTubeInitializer)
                                },
                            ) {
                                Icon(Icons.Default.Refresh, "Reload Video")
                            }

                            AndroidView(
                                factory = { context ->

                                    FrameLayout(context).apply {
                                        // select any R.id.X from your project, it does not matter what it is, but container must have one for transaction below.
                                        id = R.id.player_frame

                                        scope.launch(Dispatchers.Default) {
                                            MediaDetailViewModel.lock(uuid)

                                            youtubeView = YouTubePlayerFragment()

                                            currContext = context

                                            (context as ComponentActivity).fragmentManager
                                                .beginTransaction()
                                                .add(
                                                    id,
                                                    youtubeView,
                                                    null
                                                )
                                                .commit()

                                            youtubeView?.initialize(YOUTUBE_KEY, youTubeInitializer)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16 / 9F),
                            )
                        }

                        DisposableEffect(key1 = true) {
                            onDispose {
                                mediaDetailViewModel.currentDur = player?.currentTimeMillis
                                mediaDetailViewModel.playing = player?.isPlaying ?: false
                                player?.release()
                                //youtubeView?.onDestroy()

                                MediaDetailViewModel.unLock(uuid)
                            }
                        }
                    } else {
                        currentMedia.backdropPath?.let { backdropPath ->
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .tag("Coil request for image") // TODO
                                    .data("https://image.tmdb.org/t/p/w1280$backdropPath") // TODO: Get base url from somewhere else and size of image
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
                                    .fillMaxWidth() // TODO: Maybe move to a const
                                    .aspectRatio(16 / 9F) // TODO: Maybe move to a const
                            )
                        }
                    }
                } else {
                    currentMedia.backdropPath?.let { backdropPath ->
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .tag("Coil request for image") // TODO
                                .data("https://image.tmdb.org/t/p/w1280$backdropPath") // TODO: Get base url from somewhere else and size of image
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
                                .fillMaxWidth() // TODO: Maybe move to a const
                                .aspectRatio(16 / 9F) // TODO: Maybe move to a const
                        )
                    }
                }
            }

            item(span = { span }) {
                Row {
                    currentMedia.posterPath?.let { posterPath ->
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .tag("Coil request for image") // TODO
                                .data("https://image.tmdb.org/t/p/w154$posterPath") // TODO: Get base url from somewhere else
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
                                .width(140.dp) // TODO: Maybe move to a const
                                .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                                .padding(8.dp)
                        )
                    } ?: Image(
                        imageVector = if (currentMedia.type == "movie")
                            Icons.Default.LocalMovies
                        else
                            Icons.Default.Tv,
                        contentDescription = null,
                        modifier = Modifier
                            .width(140.dp) // TODO: Maybe move to a const
                            .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                            .padding(8.dp)
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = spacedBy(8.dp),
                    ) {
                        Text(
                            text = currentMedia.title,
                            style = MaterialTheme.typography.headlineMedium,
                        )

                        Text(
                            text = stringResource(
                                id = if (currentMedia.type == "movie")
                                    R.string.movie
                                else
                                    R.string.show,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }


            item(span = { span }) {
                LongWatchlistButton(
                    inWatchlist = currentMedia.isFavorite, modifier = Modifier.wrapContentSize(
                        Alignment.CenterStart
                    )
                ) {
                    mediaDetailViewModel.watchlistClick(currentMedia.id, currentMedia.type)
                }
            }

            item(span = { span }) {
                Text(
                    text = "Release Date: ${currentMedia.releaseDate}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp),
                )
            }

            item(span = { span }) {
                Text(
                    text = "Rating: ${"%.1f".format(currentMedia.voteAverage)}/10",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp),
                )
            }

            item(span = { span }) {
                Text(
                    text = currentMedia.genres.joinToString(" - ") { it.name },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp),
                )
            }

            mediaDetailUiState?.topCastAndDirector?.let { topCastAndDirector ->

                topCastAndDirector.director?.let { director ->
                    item(span = { GridItemSpan(10) }) {
                        Text(
                            text = "Director: ${director.name}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }

                if (topCastAndDirector.cast.isNotEmpty()) {
                    item(span = { GridItemSpan(10) }) {
                        Text(
                            text = "Cast:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp),
                        )
                    }

                    item(span = { GridItemSpan(10) }) {
                        LazyRow(
                            horizontalArrangement = spacedBy(8.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(
                                items = topCastAndDirector.cast,
                                key = { credit ->
                                    credit.id
                                },
                            ) { credit ->
                                // TODO: Maybe move to common for reuse

                                Card(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable {
                                            personClick(credit.id)
                                        }
                                ) {
                                    credit.profilePath?.let {
                                        var retryHash by remember { mutableStateOf(0) }

                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data("https://image.tmdb.org/t/p/w185${credit.profilePath}")
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
                                                .width(140.dp)
                                                .aspectRatio(2 / 3F),
                                            contentScale = ContentScale.FillWidth,
                                        )
                                    } ?: Image(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(140.dp)
                                            .aspectRatio(2 / 3F),
                                        contentScale = ContentScale.FillWidth,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                    )

                                    Spacer(modifier = Modifier.size(4.dp))

                                    Text(
                                        text = credit.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp),
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                    )

                                    Text(
                                        text = credit.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(span = { span }) {
                Text(
                    text = "Overview:\n${currentMedia.overview}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp),
                )
            }

            if ((moreItems != null && moreItems.itemCount > 0) || (favorites != null && favorites.value.isNotEmpty())) {

                item(span = { span }) {
                    Card(
                        modifier = Modifier.padding(all = 8.dp)
                    ) {
                        Text(
                            text = "Discover More",
                            modifier = Modifier
                                .padding(
                                    all = 16.dp,
                                ),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }
                }

                moreItems?.let {
                    items(
                        lazyPagingItems = moreItems,
                    ) { item ->
                        item?.let {
                            MediaCard(
                                item = item.mediaCardItem,
                                watchlistCallback = {
                                    mediaDetailViewModel.watchlistClick(
                                        item.id,
                                        item.type
                                    )
                                },
                                modifier = Modifier.padding(8.dp),
                                onClickCallback = {
                                    if (item.id != mediaId)
                                        onMediaClick(
                                            item.id,
                                            item.type,
                                            detailsOrigin,
                                            queryOrCategory,
                                            item.position
                                        )
                                }
                            )
                        }
                    }
                }

                favorites?.let { favoritesState ->
                    items(
                        items = favoritesState.value,
                    ) { item ->
                        item.map { moreItem ->
                            MediaCard(
                                item = moreItem.mediaCardItem,
                                watchlistCallback = {
                                    mediaDetailViewModel.watchlistClick(
                                        moreItem.id,
                                        moreItem.type
                                    )
                                },
                                modifier = Modifier.padding(8.dp),
                                onClickCallback = {
                                    if (moreItem.id != mediaId)
                                        onMediaClick(
                                            moreItem.id,
                                            moreItem.type,
                                            detailsOrigin,
                                            queryOrCategory,
                                            moreItem.position
                                        )
                                }
                            )
                        } // TODO: Handle load failure from the result
                    }
                }
            }
        }
    }
}

// TODO: Figure out how to save position by key or something
@ExperimentalFoundationApi
private fun <T : Any> LazyGridScope.items(
    lazyPagingItems: LazyPagingItems<T>,
    itemContent: @Composable LazyGridItemScope.(value: T?) -> Unit
) {
    items(
        count = lazyPagingItems.itemCount
    ) { index ->
        itemContent(lazyPagingItems[index])
    }
}