package com.shayo.moviesbeforetv.tv

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.shayo.movies.Movie
import com.shayo.movies.MoviesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class TrailerPlayer : Fragment() {

    @Inject
    lateinit var moviesRepository: MoviesRepository

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

        val videoId = navArgs<TrailerPlayerArgs>().value.youtubeId // TODO:
        val movie = navArgs<TrailerPlayerArgs>().value.movie

        val adapter = MyAdapter(videoId)

        player.initialize(adapter, IFramePlayerOptions.Builder().controls(0).ivLoadPolicy(3).build())

        val controls = view.findViewById<FragmentContainerView>(R.id.controls)

        controls.getFragment<VideoFragment>().setMovie(adapter, movie) {
            lifecycleScope.launch {
                moviesRepository.toggleFavorite(movie)
            }
        }

        player.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                player.visibility = View.VISIBLE
            }
        })

        lifecycleScope.launch {
            moviesRepository.favoritesMap.collectLatest {
                controls.getFragment<VideoFragment>().updateIsFavorite(it.containsKey(movie.id))
            }
        }
    }
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

private class MyGlue<T: PlayerAdapter>(
    context: Context?,
    impl: T,
    private val watchlistCallback: () -> Unit
) : PlaybackTransportControlGlue<T>(context,impl) {
    private val thumbsUpAction = PlaybackControlsRow.ThumbsUpAction(context)

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
        super.onCreatePrimaryActions(primaryActionsAdapter)
        primaryActionsAdapter.apply {
            add(thumbsUpAction)
        }
    }

    override fun onActionClicked(action: Action) {
        when(action) {
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

        playerGlue?.host = PlaybackSupportFragmentGlueHost(this)

        playerGlue?.title = "${movie.title} Trailer"

        playerGlue?.subtitle = movie.overview

        movie.posterPath?.let {
            lifecycleScope.launch {
                playerGlue?.art =
                    drawableFromUrl("https://image.tmdb.org/t/p/w500${movie.posterPath}")
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
        })
    }

    private suspend fun drawableFromUrl(url: String): Drawable {
        return withContext(Dispatchers.IO) {
            val x: Bitmap
            val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val input: InputStream = connection.inputStream
            x = BitmapFactory.decodeStream(input)
            BitmapDrawable(Resources.getSystem(), x)
        }
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

class MyAdapter(private val videoId: String) : PlayerAdapter(), YouTubePlayerListener {

    private var youtubePlayer: YouTubePlayer? = null

    private var second: Float = 0F
    private var duration: Float = 0F
    private var playing: Boolean = false
    private var buffered: Float = 0F
    private var ready = false

    override fun play() {
        youtubePlayer?.play()
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
        youTubePlayer.loadVideo(videoId, 0F)
        ready = true
        callback.onPreparedStateChanged(this)
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