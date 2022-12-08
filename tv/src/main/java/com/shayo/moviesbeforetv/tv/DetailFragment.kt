package com.shayo.moviesbeforetv.tv

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.shayo.movies.Movie
import com.shayo.movies.MoviesRepository
import com.shayo.movies.VideoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : DetailsSupportFragment() {

    @Inject
    lateinit var moviesRepository: MoviesRepository

    @Inject
    lateinit var videoRepository: VideoRepository

    private var trailer: String? = null

    private lateinit var mBackgroundManager: BackgroundManager
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)

        val movie = navArgs<DetailFragmentArgs>().value.movie

        mBackgroundUri = movie.backdropPath

        val mPresenterSelector = ClassPresenterSelector()

        val mAdapter = ArrayObjectAdapter(mPresenterSelector)


        val row = DetailsOverviewRow(movie)

        Glide.with(requireActivity())
            .load("https://image.tmdb.org/t/p/original/${movie.posterPath}")
            .centerCrop()
            .into<SimpleTarget<Drawable>>(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    drawable: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    row.imageDrawable = drawable
                    mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                }
            })

        val actionAdapter = ArrayObjectAdapter()

        lifecycleScope.launch {

            videoRepository.getTrailer(movie.type, movie.id).onSuccess {
                it?.apply {
                    trailer = it.key

                    actionAdapter.add(
                        Action(
                            0,
                            "Watch Trailer",
                            ""
                        )
                    )
                }
            }
        }

        val watchListAction = Action(
            1,
            "Add To Watchlist",
            ""
        )

        actionAdapter.add(
            watchListAction
        )

        lifecycleScope.launch {
            moviesRepository.favoritesMap.collectLatest {

                watchListAction.label1 = if (it.containsKey(movie.id)) {
                    "Remove From Watchlist"
                } else {
                    "Add To Watchlist"
                }

                actionAdapter.replace(
                    0, watchListAction
                )
            }
        }

        row.actionsAdapter = actionAdapter

        mAdapter.add(row)


        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())

        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
        detailsPresenter.backgroundColor =
            ContextCompat.getColor(requireActivity(), R.color.brand_color)

        lifecycleScope.launch { // TODO:
            /*val listRowAdapter = ArrayObjectAdapter(CardPresenter())

            val movies = moviesRepository.getMovies("popular").getOrNull()!!

            movies.forEach {
                if (it.id != movie.id)
                    listRowAdapter.add(it)
            }

            val header = HeaderItem(0, "More In The Category:")
            mAdapter.add(ListRow(header, listRowAdapter))
            mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())


            adapter = mAdapter*/
        }

        adapter = mAdapter

        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            when (action.id) {

                1L -> {
                    lifecycleScope.launch {
                        moviesRepository.toggleFavorite(movie)
                    }
                }
                0L -> {
                    findNavController().navigate(
                        DetailFragmentDirections.actionDetailFragmentToTrailerPlayer(
                            trailer!!,
                            movie
                        )
                    )
                }
            }
        }
    }

    private fun updateBackground(uri: String?) {
        if (uri != null) {

            val width = mMetrics.widthPixels
            val height = mMetrics.heightPixels
            Glide.with(requireActivity())
                .load("https://image.tmdb.org/t/p/original/$uri")
                .centerCrop()
                .error(R.drawable.ic_baseline_movie_filter_24)
                .into<SimpleTarget<Drawable>>(
                    object : SimpleTarget<Drawable>(width, height) {
                        override fun onResourceReady(
                            drawable: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            mBackgroundManager.drawable = drawable
                        }
                    })
        } else mBackgroundManager.drawable = null
        mBackgroundTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()

        updateBackground(mBackgroundUri)
    }
}

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any
    ) {
        val movie = item as Movie

        viewHolder.title.text = movie.title

        viewHolder.subtitle.maxLines = 3

        viewHolder.subtitle.text = "${movie.releaseDate}\n${movie.voteAverage}/10\n" +
                "${movie.genres.joinToString(" - ") { it.name }}"



        viewHolder.body.text = movie.overview
    }
}