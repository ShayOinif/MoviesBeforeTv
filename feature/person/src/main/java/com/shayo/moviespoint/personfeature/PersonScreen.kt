package com.shayo.moviespoint.personfeature

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.LocalMovies
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.shayo.movies.Movie
import com.shayo.moviespoint.ui.LongWatchlistButton

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun PersonScreen(
    personId: Int,
    onMediaClicked: (mediaId: Int, mediaType: String) -> Unit,
    personViewModel: PersonViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = true) {
        personViewModel.setPersonId(personId)
    }

    val state by personViewModel.personFlow.collectAsStateWithLifecycle()

    when (state) {
        is PersonUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is PersonUiState.Error -> {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Error:${(state as PersonUiState.Error).message?.let { " $it" } ?: ""}, Retry?",
                )

                Spacer(modifier = Modifier.size(8.dp))

                IconButton(
                    onClick = (state as PersonUiState.Error).retry,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.filledIconButtonColors(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry Loading",
                    )
                }
            }
        }
        is PersonUiState.Success -> {

            val person = (state as PersonUiState.Success).person

            Column(
                verticalArrangement = spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {

                person.combinedCredits.cast.maxByOrNull { movie ->
                    movie.popularity ?: 0.0
                }?.backdropPath?.let { backdropPath ->
                    var retryHash by remember { mutableStateOf(0) }

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .tag("Coil request for image") // TODO
                            .data("https://image.tmdb.org/t/p/w1280$backdropPath") // TODO: Get base url from somewhere else and size of image
                            .crossfade(true)
                            .setParameter(
                                "retry_hash",
                                retryHash,
                                memoryCacheKey = null
                            )
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
                            .aspectRatio(16 / 9F) // TODO: Maybe move to a const
                    )
                }

                Row {
                    person.profilePath?.let { profilePath ->
                        var retryHash by remember { mutableStateOf(0) }

                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .tag("Coil request for image") // TODO
                                .data("https://image.tmdb.org/t/p/w185$profilePath")
                                .crossfade(true)
                                .setParameter(
                                    "retry_hash",
                                    retryHash,
                                    memoryCacheKey = null
                                )
                                .build(),
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
                                .aspectRatio(2 / 3F)
                                .padding(horizontal = 8.dp),
                            contentScale = ContentScale.FillWidth,
                        )
                    } ?: run {
                        Image(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .width(80.dp) // TODO: Maybe move to a const
                                .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                                .padding(horizontal = 8.dp),
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = spacedBy(8.dp),
                    ) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.headlineMedium,
                        )

                        person.knownForDepartment?.let { knownForDepartment ->
                            Text(
                                text = stringResource(id = R.string.known_for, knownForDepartment),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }

                Text(
                    text = "Bio:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )


                var expanded by rememberSaveable { mutableStateOf(false) }

                AnimatedContent(targetState = expanded) { currentExpanded ->
                    Text(
                        text = person.biography,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                expanded = !expanded
                            },
                        maxLines = if (currentExpanded) Int.MAX_VALUE else 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = "Filmography:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                Spacer(Modifier.size(8.dp))

                MediaRow(
                    medias = if (person.knownForDepartment == "Acting") {
                        person.combinedCredits.cast.sortedByDescending { movie ->
                            movie.releaseDate
                        }.map { media ->
                            MediaPerson(media, media.desc)
                        }
                    } else {
                        person.combinedCredits.crew.sortedByDescending { movie ->
                            movie.releaseDate
                        }.map { media ->
                            MediaPerson(media, media.desc)
                        }
                    },
                    watchlistCallback = personViewModel::watchlistClick,
                    onMediaClick = onMediaClicked
                )
            }
        }
    }
}

internal data class MediaPerson(
    val media: Movie,
    val desc: String?,
)

@Composable
internal fun MediaRow(
    medias: List<MediaPerson>,
    watchlistCallback: (id: Int, type: String) -> Unit,
    onMediaClick: (mediaId: Int, mediaType: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        items(
            items = medias,
            key = { mediaPerson ->
                "${mediaPerson.media.id}${mediaPerson.desc}"
            },
        ) { mediaPerson ->
            // TODO: Move this mapping to view model to save memory and such,

            MediaCreditCard(
                mediaPerson = mediaPerson,
                watchlistCallback = {
                    watchlistCallback(
                        mediaPerson.media.id,
                        mediaPerson.media.type
                    )
                },
                onClickCallback = { onMediaClick(mediaPerson.media.id, mediaPerson.media.type) }
            )
        }
    }
}

@Composable
internal fun MediaCreditCard(
    mediaPerson: MediaPerson,
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

        mediaPerson.media.posterPath?.let {
            var retryHash by remember { mutableStateOf(0) }

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .tag("Coil request for image") // TODO
                    .data("https://image.tmdb.org/t/p/w154${mediaPerson.media.posterPath}") // TODO: Get base url from somewhere else
                    .crossfade(true)
                    .setParameter(
                        "retry_hash",
                        retryHash,
                        memoryCacheKey = null
                    )
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
        } ?: Image(
            imageVector = Icons.Outlined.LocalMovies,
            contentDescription = null,
            modifier = Modifier
                .width(154.dp) // TODO: Maybe move to a const
                .aspectRatio(2 / 3F) // TODO: Maybe move to a const
                .padding(horizontal = 16.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )

        Text(
            text = mediaPerson.media.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        mediaPerson.desc?.let { character ->
            Text(
                text = character.ifEmpty { "No Character" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            mediaPerson.media.releaseDate?.let { releaseDate ->
                Text(
                    text = releaseDate.take(4),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = if (mediaPerson.media.type == "movie")
                    Icons.Default.LocalMovies
                else
                    Icons.Default.Tv,
                contentDescription = if (mediaPerson.media.type == "movie")
                    "Movie"
                else
                    "TV Show",
            )
        }

        LongWatchlistButton(
            inWatchlist = mediaPerson.media.isFavorite,
            watchlistCallback = watchlistCallback
        )

        Spacer(Modifier.size(2.dp))
    }
}