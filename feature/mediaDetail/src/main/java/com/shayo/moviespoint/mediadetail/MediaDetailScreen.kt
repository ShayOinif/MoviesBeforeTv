package com.shayo.moviespoint.mediadetail

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.shayo.moviespoint.ui.LongWatchlistButton

@SuppressLint("ResourceType")
@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
internal fun MediaDetailScreen(
    //modifier: Modifier = Modifier,
    mediaId: Int,
    mediaType: String,
    mediaDetailViewModel: MediaDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = true) {
        mediaDetailViewModel.setMedia(mediaId, mediaType)
    }

    val mediaDetailUiState by mediaDetailViewModel.detailsFlow.collectAsStateWithLifecycle()

    mediaDetailUiState?.media?.let { currentMedia ->
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            mediaDetailUiState?.videoId?.let { key ->
                var player: YouTubePlayer? = null

                DisposableEffect(key1 = true) {
                    onDispose {
                        player?.release()
                    }
                }

                AndroidView(
                    factory = {
                        val youtubeApiInitializedListener = object : YouTubePlayer.OnInitializedListener {
                            override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer?, p2: Boolean) {
                                player = p1
                                p1?.cueVideo(key)
                            }

                            override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
                                // TODO:
                            }
                        }

                        FrameLayout(it).apply {
                            // select any R.id.X from your project, it does not matter what it is, but container must have one for transaction below.
                            id = 99999

                            val youtubeView = YouTubePlayerFragment()

                            (it as ComponentActivity).fragmentManager
                                .beginTransaction()
                                .add(
                                    id,
                                    youtubeView,
                                    null
                                )
                                .commit()

                            youtubeView.initialize("1", youtubeApiInitializedListener)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            } ?: currentMedia.backdropPath?.let { backdropPath ->
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
            } ?: run {
                // TODO:
            }

            Row {
                currentMedia.posterPath?.let { posterPath ->
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).tag("Coil request for image") // TODO
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
                            .width(80.dp) // TODO: Maybe move to a const
                            .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                            .padding(8.dp)
                    )
                } ?: run {
                    Image(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = null,
                        modifier = Modifier
                            .width(80.dp) // TODO: Maybe move to a const
                            .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                            .padding(8.dp)
                    )
                }

                Text(
                    text = currentMedia.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp),
                )
            }


            LongWatchlistButton(inWatchlist = currentMedia.isFavorite) {
                mediaDetailViewModel.watchlistClick(currentMedia.id, currentMedia.type)
            }

            Text(
                text = "Release Date: ${currentMedia.releaseDate}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp),
            )

            Text(
                text = "Rating: ${currentMedia.voteAverage}/10",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp),
            )

            Text(
                text = "${currentMedia.genres.joinToString(" - ") { it.name }}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp),
            )

            mediaDetailUiState?.topCastAndDirector?.let { topCastAndDirector ->

                topCastAndDirector.director?.let { director ->
                    Text(
                        text = "Director: ${director.name}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp),
                    )
                }

                Text(
                    text = "Cast:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp),
                )

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
                                    // TODO:
                                }
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .tag("Coil request for image") // TODO
                                    .data("https://image.tmdb.org/t/p/w185${credit.profilePath}")
                                    .crossfade(true)
                                    .build(),
                                error = {
                                    Icon(
                                        Icons.Default.BrokenImage,
                                        null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                },
                                contentDescription = null,
                                modifier = Modifier
                                    .width(140.dp)
                                    .aspectRatio(2 / 3F),
                                contentScale = ContentScale.FillWidth,
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

            Text(
                text = "Overview:\n${currentMedia.overview}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}