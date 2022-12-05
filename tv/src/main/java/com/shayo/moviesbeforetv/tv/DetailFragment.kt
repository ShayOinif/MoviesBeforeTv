package com.shayo.moviesbeforetv.tv

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
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
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : DetailsSupportFragment() {

    @Inject
    lateinit var moviesRepository: MoviesRepository

    @Inject
    lateinit var videoRepository: VideoRepository

    private var trailer: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val movie = navArgs<DetailFragmentArgs>().value.movie

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

            videoRepository.getTrailer(movie.id).onSuccess {
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
}

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any
    ) {
        val movie = item as Movie

        viewHolder.title.text = movie.title
        viewHolder.subtitle.text = movie.releaseDate
        viewHolder.body.text = "${movie.genres.joinToString(" - ") { it.name }}\n${movie.overview}"
    }
}