package com.shayo.moviespoint.mediadetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.map
import com.google.android.youtube.player.YouTubePlayer
import com.shayo.movies.*
import com.shayo.moviespoint.ui.DetailsOrigin
import com.shayo.moviespoint.ui.MediaCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.*
import javax.inject.Inject

/* TODO: Move this viewmodel to it's own module and reuse in the Tv module.
 *  Especially make it more general and let the UI map the data depending on it's
 *  needs. Right now the more item is quite similar to BrowseLoadResult in the Tv module.
 *
 * Also move mappings in all viewmodel upwards, cause we cache again after the mapping, too many caching.
 */
@HiltViewModel
internal class MediaDetailViewModel @Inject constructor(
    private val movieManager: MovieManager,
    private val creditsRepository: CreditsRepository,
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val detailParamsFlow = MutableStateFlow<DetailParams?>(null)

    fun setMedia(
        mediaId: Int,
        mediaType: String,
        detailsOrigin: DetailsOrigin,
        queryOrCategory: String?,
        position: Int?
    ) {
        detailParamsFlow.value =
            DetailParams(mediaId, mediaType, detailsOrigin, queryOrCategory, position)
    }

    var currentVideo = 0
    var currentDur: Int? = null
    var playing = false

    var currentPlayer: YouTubePlayer? = null

    companion object {
        private val mutex = Mutex()

        suspend fun lock(id: UUID) {
            mutex.lock(id)
        }

        fun unLock(id: UUID) {
            if (mutex.holdsLock(id) && mutex.isLocked)
                mutex.unlock(id)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoritesFlow = detailParamsFlow.flatMapLatest { detailParams ->
        if (detailParams?.detailsOrigin == DetailsOrigin.WATCHLIST) {
            movieManager.getFavoritesFlow(withGenres = false)
                .mapLatest { resultList ->
                    resultList.map { result ->
                        result.map { media ->
                            with(media) {
                                MoreItem(
                                    id, type,
                                    MediaCardItem(
                                        posterPath,
                                        title,
                                        "%.1f".format(voteAverage),
                                        releaseDate,
                                        inWatchlist = isFavorite,
                                        type = type
                                    ),
                                    position = 0,
                                )
                            }
                        }
                    }
                }
        } else emptyFlow()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val moreFlow = detailParamsFlow.flatMapLatest { detailParams ->
        if (detailParams?.detailsOrigin == DetailsOrigin.CATEGORY) {
            movieManager.getCategoryFlow(
                type = detailParams.mediaType,
                category = detailParams.queryOrCategory!!, // TODO:
                scope = viewModelScope,
                position = detailParams.position!!, // TODO:
                withGenres = false,
            ).mapLatest { pagedItem ->
                pagedItem.map { pagedMovie ->
                    with(pagedMovie.movie) {
                        MoreItem(
                            id, type,
                            MediaCardItem(
                                posterPath,
                                title,
                                "%.1f".format(voteAverage),
                                releaseDate,
                                inWatchlist = isFavorite,
                                type = type
                            ),
                            position = pagedMovie.position
                        )
                    }
                }
            }
        } else flow {
            PagingData.empty<MoreItem>(
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(true),
                    prepend = LoadState.NotLoading(true),
                    append = LoadState.NotLoading(true)
                )
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PagingData.empty<MoreItem>(
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(true),
                prepend = LoadState.NotLoading(true),
                append = LoadState.NotLoading(true)
            )
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailsFlow = detailParamsFlow.flatMapLatest { detailParams ->
        detailParams?.let {
            movieManager.getDetailedMovieByIdFlow(detailParams.mediaId, detailParams.mediaType)
                .map {
                    MediaDetailUiState(
                        media = it,
                        topCastAndDirector = creditsRepository.getCredits(
                            detailParams.mediaType,
                            detailParams.mediaId
                        ).fold(
                            onSuccess = { topCastAndDirector ->
                                topCastAndDirector
                            },
                            onFailure = {
                                null
                            }
                        ),
                        videoIds = videoRepository.getTrailers(
                            detailParams.mediaType,
                            detailParams.mediaId
                        ).fold(
                            onSuccess = { trailers ->
                                trailers.map { video ->
                                    video.key
                                }
                            },
                            onFailure = {
                                emptyList()
                            }
                        )
                    )
                }
        } ?: emptyFlow()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    fun watchlistClick(id: Int, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            movieManager.toggleFavorite(id, type)
        }
    }
}

internal data class MoreItem(
    val id: Int,
    val type: String,
    val mediaCardItem: MediaCardItem,
    val position: Int,
)

internal data class MediaDetailUiState(
    val media: Movie?, // TODO: Map only to necessary for the screen
    val topCastAndDirector: TopCastAndDirector?,
    val videoIds: List<String>,
)

private data class DetailParams(
    val mediaId: Int,
    val mediaType: String,
    val detailsOrigin: DetailsOrigin,
    val queryOrCategory: String?,
    val position: Int?,
)