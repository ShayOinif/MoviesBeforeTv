package com.shayo.moviesbeforetv.tv

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.leanback.app.PlaybackSupportFragment
import androidx.leanback.app.PlaybackSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.media.PlayerAdapter
import androidx.leanback.widget.*
import androidx.leanback.widget.PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE
import androidx.leanback.widget.PlaybackControlsRow.ThumbsAction.INDEX_SOLID
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.shayo.movies.Movie
import com.shayo.movies.MovieManager
import com.shayo.movies.MoviesRepository
import com.shayo.movies.VideoRepository
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrailerPlayer : Fragment() {

    @Inject
    lateinit var movieManager: MovieManager

    @Inject
    lateinit var movieRepository: MoviesRepository

    @Inject
    lateinit var videoRepository: VideoRepository

    private var initialSetup = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Trailer - TV")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val player = view.findViewById<YouTubePlayerView>(R.id.youtube_player_view)

        viewLifecycleOwner.lifecycle.addObserver(player)

        val navArgs by navArgs<TrailerPlayerArgs>()

        val controls = view.findViewById<FragmentContainerView>(R.id.controls)

        if (initialSetup) {
            viewLifecycleOwner.lifecycleScope.launch {
                //launch {
                    movieRepository.getMovieById(navArgs.movieId, navArgs.movieType).fold(
                        onSuccess = { movie ->
                            videoRepository.getTrailers(movie.type, movie.id).fold(
                                onSuccess = { trailers ->

                                    val adapter = MyAdapter(trailers.map { video -> video.key })

                                    player.initialize(
                                        adapter,
                                        IFramePlayerOptions.Builder().controls(0).ivLoadPolicy(3)
                                            .build()
                                    )

                                    controls.getFragment<VideoFragment>().setMovie(adapter, movie) {
                                        viewLifecycleOwner.lifecycleScope.launch {
                                            movieManager.toggleFavorite(movie.id, movie.type)
                                        }
                                    }

                                    viewLifecycleOwner.lifecycleScope.launch {
                                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                            movieManager.favoritesMap.collectLatest {
                                                controls.getFragment<VideoFragment>()
                                                    .updateIsFavorite(it.containsKey(navArgs.movieId))
                                            }
                                        }
                                    }

                                    player.getYouTubePlayerWhenReady(object :
                                        YouTubePlayerCallback {
                                        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                            player.visibility = View.VISIBLE
                                        }
                                    })
                                },
                                onFailure = ::navToError,
                            )
                        },
                        onFailure = ::navToError,
                    )
               // }
            }
        }
    }
}

private fun TrailerPlayer.navToError(error: Throwable) {
    findNavController().navigate(
        TrailerPlayerDirections.actionTrailerPlayerToErrorFragment(
            message = error.message ?: "Unknown"
        )
    )
}

class MyCustomDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(vh: ViewHolder?, item: Any?) {
        vh?.subtitle?.maxLines = 10

        if (item is MyGlue<*>) {
            vh?.title?.text = item.title
            vh?.subtitle?.text = item.subtitle
        }
    }
}

private class MyGlue<T : PlayerAdapter>(
    context: Context?,
    impl: T,
    private val watchlistCallback: () -> Unit
) : PlaybackTransportControlGlue<T>(context, impl) {
    private val thumbsUpAction = PlaybackControlsRow.ThumbsUpAction(context)
    private val skipPreviousAction = PlaybackControlsRow.SkipPreviousAction(context)
    private val skipNextAction = PlaybackControlsRow.SkipNextAction(context)


    override fun onCreateRowPresenter(): PlaybackRowPresenter {
        return super.onCreateRowPresenter().apply {
            (this as? PlaybackTransportRowPresenter)
                ?.setDescriptionPresenter(MyCustomDescriptionPresenter())
        }
    }

    override fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter) {
        // Order matters, super.onCreatePrimaryActions() will create the play / pause action.
        // Will display as follows:
        // play/pause, previous, rewind, fast forward, next
        //   > /||      |<        <<        >>         >|

        primaryActionsAdapter.apply {
            add(skipPreviousAction)
            super.onCreatePrimaryActions(primaryActionsAdapter)
            add(skipNextAction)
        }
    }

    override fun onCreateSecondaryActions(adapter: ArrayObjectAdapter?) {
        super.onCreateSecondaryActions(adapter)
        adapter?.apply {
            add(thumbsUpAction)
        }
    }

    override fun onActionClicked(action: Action) {
        when (action) {
            thumbsUpAction -> {
                watchlistCallback()
            }
            else -> super.onActionClicked(action)
        }
    }

    fun updateIsFavorite(isFavorite: Boolean) {
        thumbsUpAction.index = if (isFavorite) INDEX_SOLID else INDEX_OUTLINE

        host.notifyPlaybackRowChanged()
    }
}

class VideoFragment : PlaybackSupportFragment() {

    private var playerGlue: MyGlue<PlayerAdapter>? = null

    fun updateIsFavorite(isFavorite: Boolean) {
        playerGlue?.updateIsFavorite(isFavorite)
    }

    fun setMovie(adapter: MyAdapter, movie: Movie, watchlistCallback: () -> Unit) {

        playerGlue = MyGlue(
            context,
            adapter,
            watchlistCallback
        )

        adapter.getCurrentListPosition { current, total ->
            playerGlue?.title = "${movie.title} Trailer $current out of $total"

            // TODO: Figure out why it does not update the watch next channel
            /*val builder = WatchNextProgram.Builder()
            builder
                .setWatchNextType(TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_NEXT)
                .setTitle("Title")
                .setDescription("Program description")
                .setPosterArtUri(Uri.parse("https://image.tmdb.org/t/p/w500${movie.posterPath}"))

            val watchNextProgramUri = context?.contentResolver?.insert(
                TvContractCompat.WatchNextPrograms.CONTENT_URI,
                builder.build().toContentValues())

            Log.d("MyTAg", "$watchNextProgramUri")*/
        }

        playerGlue?.host = PlaybackSupportFragmentGlueHost(this)

        playerGlue?.subtitle = movie.overview

        movie.posterPath?.let {
            lifecycleScope.launch {
                playerGlue?.art = loadDrawable(
                    this@VideoFragment,
                    "https://image.tmdb.org/t/p/w500${movie.posterPath}"
                )
            }
        }

        playerGlue?.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
            override fun onPreparedStateChanged(glue: PlaybackGlue) {
                if (glue.isPrepared) {
                    playerGlue?.let {
                        playerGlue?.seekProvider = MySeekProvider(it.duration)
                    }
                }
            }

            override fun onPlayCompleted(glue: PlaybackGlue?) {
                super.onPlayCompleted(glue)

                glue?.next()
            }
        })
    }
}

private class MySeekProvider(duration: Long) : PlaybackSeekDataProvider() {

    val position = LongArray((duration / 10_000).toInt()) {
        (it.toLong() + 1) * 10_000
    }

    override fun getSeekPositions(): LongArray {
        return position
    }
}

class MyAdapter(private val videoIds: List<String>) : PlayerAdapter(), YouTubePlayerListener {

    private var youtubePlayer: YouTubePlayer? = null

    private var second: Float = 0F
    private var duration: Float = 0F
    private var playing: Boolean = false
    private var buffered: Float = 0F
    private var ready = false
    private var listPositionCallback: ((position: Int, total: Int) -> Unit)? = null

    private var currentPosition = 0

    override fun play() {
        youtubePlayer?.play()
    }

    fun getCurrentListPosition(callback: (position: Int, total: Int) -> Unit) {
        listPositionCallback = callback
    }

    override fun pause() {
        youtubePlayer?.pause()
    }

    override fun getCurrentPosition(): Long {
        return second.toLong() * 1000
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {

        this.second = second

        callback.onCurrentPositionChanged(this)
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
        callback.onError(this, error.hashCode(), error.name)
    }

    override fun onPlaybackQualityChange(
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality
    ) {
    }

    override fun onPlaybackRateChange(
        youTubePlayer: YouTubePlayer,
        playbackRate: PlayerConstants.PlaybackRate
    ) {
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {

        this.youtubePlayer = youTubePlayer
        youTubePlayer.loadVideo(videoIds.first(), 0F)

        listPositionCallback?.invoke(currentPosition + 1, videoIds.size)

        ready = true
        callback.onPreparedStateChanged(this)
    }

    override fun next() {
        super.next()

        if (videoIds.size - 1 > currentPosition) {
            youtubePlayer?.loadVideo(videoIds[++currentPosition], 0F)

            listPositionCallback?.invoke(currentPosition + 1, videoIds.size)
        }
    }

    override fun previous() {
        super.previous()

        if (currentPosition > 0) {
            youtubePlayer?.loadVideo(videoIds[--currentPosition], 0F)

            listPositionCallback?.invoke(currentPosition + 1, videoIds.size)
        } else {
            seekTo(0)
        }
    }

    override fun isPlaying(): Boolean {
        return playing
    }

    override fun onStateChange(
        youTubePlayer: YouTubePlayer,
        state: PlayerConstants.PlayerState
    ) {
        when (state) {
            PlayerConstants.PlayerState.PAUSED -> {
                playing = false
                callback.onPlayStateChanged(this)
            }
            PlayerConstants.PlayerState.PLAYING -> {
                playing = true
                callback.onPlayStateChanged(this)
            }
            PlayerConstants.PlayerState.ENDED -> {
                playing = false

                callback.onPlayCompleted(this)
            }
            else -> {}
        }
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {

        this.duration = duration

        callback.onDurationChanged(this)
    }

    override fun getDuration(): Long {
        return duration.toLong() * 1000
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
    }

    override fun onVideoLoadedFraction(
        youTubePlayer: YouTubePlayer,
        loadedFraction: Float
    ) {
        buffered = loadedFraction * duration
        callback.onBufferedPositionChanged(this)
    }

    override fun isPrepared(): Boolean {
        return ready
    }

    override fun seekTo(positionInMs: Long) {
        youtubePlayer?.seekTo(positionInMs / 1000F)
    }

    override fun getBufferedPosition(): Long {
        return buffered.toLong() * 1000
    }
}