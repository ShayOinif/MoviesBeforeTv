package com.shayo.moviespoint.mediadetail

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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

@SuppressLint("ResourceType")
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
    var detailsFlow = detailParamsFlow.flatMapLatest { detailParams ->
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
                        ),
                        moreFlow = when (detailParams.detailsOrigin) {
                            DetailsOrigin.CATEGORY -> {
                                movieManager.getCategoryFlow(
                                    type = detailParams.mediaType,
                                    category = detailParams.queryOrCategory!!, // TODO:
                                    scope = viewModelScope,
                                    position = detailParams.position!! // TODO:
                                ).map { pagedItem ->
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
                                }.cachedIn(viewModelScope)
                            }
                            else -> {
                                null
                            }
                        } // TODO:
                    )
                }
        } ?: emptyFlow()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1_500), // TODO
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
    val media: Movie?,
    val topCastAndDirector: TopCastAndDirector?,
    val videoIds: List<String>,
    val moreFlow: Flow<PagingData<MoreItem>>?
)

private data class DetailParams(
    val mediaId: Int,
    val mediaType: String,
    val detailsOrigin: DetailsOrigin,
    val queryOrCategory: String?,
    val position: Int?,
)