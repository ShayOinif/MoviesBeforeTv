package com.shayo.moviespoint.mediadetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shayo.movies.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MediaDetailViewModel @Inject constructor(
    private val movieManager: MovieManager,
    private val creditsRepository: CreditsRepository,
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val detailParamsFlow = MutableStateFlow<DetailParams?>(null)

    fun setMedia(mediaId: Int, mediaType: String) {
        detailParamsFlow.value = DetailParams(mediaId, mediaType)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    var detailsFlow = detailParamsFlow.flatMapLatest { detailParams ->
        detailParams?.let {
            movieManager.getDetailedMovieByIdFlow(detailParams.mediaId, detailParams.mediaType).map {
                MediaDetailUiState(
                    media = it,
                    topCastAndDirector = creditsRepository.getCredits(detailParams.mediaType, detailParams.mediaId).fold(
                        onSuccess = { topCastAndDirector ->
                            topCastAndDirector
                        },
                        onFailure = {
                            null
                        }
                    ),
                    videoId = videoRepository.getTrailers(detailParams.mediaType, detailParams.mediaId).fold(
                        onSuccess = { trailers ->
                            trailers.firstOrNull()?.key
                        },
                        onFailure = {
                            null
                        }
                    ),
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

internal data class MediaDetailUiState(
    val media: Movie?,
    val topCastAndDirector: TopCastAndDirector?,
    val videoId: String?,
)

private data class DetailParams(
    val mediaId: Int,
    val mediaType: String,
)