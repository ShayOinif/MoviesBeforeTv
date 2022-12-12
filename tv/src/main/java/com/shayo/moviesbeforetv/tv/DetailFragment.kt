package com.shayo.moviesbeforetv.tv

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.map
import com.shayo.movies.*
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : DetailsSupportFragment(), FragmentWithBackground {

    @Inject
    lateinit var moviesManager: MovieManager

    @Inject
    lateinit var videoRepository: VideoRepository

    @Inject
    lateinit var creditsRepository: CreditsRepository

    private var trailer: String? = null

    override lateinit var backgroundManager: BackgroundManager

    override var backgroundFlow = MutableStateFlow<Background?>(null)

    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var actionAdapter: ArrayObjectAdapter
    private lateinit var watchListAction: Action
    private lateinit var detailsPresenter: FullWidthDetailsOverviewRowPresenter
    private val detailsRow = DetailsOverviewRow(Object())
    private lateinit var categoryAdapter: androidx.leanback.paging.PagingDataAdapter<BrowseMovieLoadResult.BrowseMovie>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mPresenterSelector = ClassPresenterSelector()
        actionAdapter = ArrayObjectAdapter()
        mAdapter = ArrayObjectAdapter(mPresenterSelector)

        watchListAction = Action(
            1,
            "Add To Watchlist",
            ""
        )

        actionAdapter.add(
            watchListAction
        )

        mAdapter.add(detailsRow)

        val category = navArgs<DetailFragmentArgs>().value.category

        category?.let {
            val header = HeaderItem("More In The Category ${category}:")

            categoryAdapter =
                androidx.leanback.paging.PagingDataAdapter(CardPresenter(resources.displayMetrics.widthPixels), MovieDiff())

            mAdapter.add(ListRow(
                header,
                categoryAdapter
            ))
        }

        mPresenterSelector.addClassPresenter(
            ListRow::class.java,
            ListRowPresenter().apply {

                setOnItemViewClickedListener { _, item, _, _ ->
                    // TODO: Reuse with the one in the browse fragment
                    if (item is BrowseMovieLoadResult.BrowseMovie && item.movie.id != navArgs<DetailFragmentArgs>().value.movieId) {
                        val action =
                            DetailFragmentDirections.actionDetailFragmentSelf(
                                item.movie.id,
                                item.movie.type,
                                category,
                                item.position
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        )

        detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())

        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
        detailsPresenter.backgroundColor =
            ContextCompat.getColor(requireActivity(), R.color.brand_color)

        lifecycleScope.launch {
            val movieId = navArgs<DetailFragmentArgs>().value.movieId
            val movieType = navArgs<DetailFragmentArgs>().value.movieType

            videoRepository.getTrailer(movieType, movieId).onSuccess {
                it?.apply {
                    trailer = it.key

                    actionAdapter.add(
                        1,
                        Action(
                            0,
                            "Watch Trailer",
                            ""
                        )
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackgroundUpdate(viewLifecycleOwner, this, requireActivity(), 0L)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val movieId = navArgs<DetailFragmentArgs>().value.movieId
                val movieType = navArgs<DetailFragmentArgs>().value.movieType
                val category = navArgs<DetailFragmentArgs>().value.category
                val position = navArgs<DetailFragmentArgs>().value.position

                // TODO: Make cache for credits so in won't fetch from network again and again
                val topCastAndDirector = creditsRepository.getCredits(movieType, movieId)

                launch {
                    moviesManager.getDetailedMovieByIdFlow(movieId, movieType)
                        .collectLatest { movie ->
                            movie?.let {

                                backgroundFlow.value = movie.backdropPath?.let {
                                    Background.HasBackground(it)
                                } ?: Background.NoBackground

                                detailsRow.item = with(movie) {
                                    DetailedMovie(
                                        title,
                                        voteAverage,
                                        genres,
                                        runtime,
                                        overview,
                                        topCastAndDirector.fold(
                                            onSuccess = {
                                                it
                                            },
                                            onFailure = {
                                                null
                                            }
                                        ),
                                        releaseDate
                                    )
                                }

                                loadDrawable(
                                    this@DetailFragment,
                                    "https://image.tmdb.org/t/p/w500/${movie.posterPath}"
                                ) { drawable ->
                                    detailsRow.imageDrawable = drawable
                                    mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                                }

                                watchListAction.label1 = if (movie.isFavorite) {
                                    "Remove From Watchlist"
                                } else {
                                    "Add To Watchlist"
                                }

                                actionAdapter.replace(
                                    0, watchListAction
                                )

                                detailsRow.actionsAdapter = actionAdapter

                                // TODO: could be removed from collect once the trailer fragment works only with ids
                                detailsPresenter.onActionClickedListener =
                                    OnActionClickedListener { action ->
                                        when (action.id) {

                                            1L -> {
                                                lifecycleScope.launch {
                                                    moviesManager.toggleFavorite(movie)
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

                                adapter = mAdapter
                            }
                        }
                }

                // TODO: Change category to origin so we could do the same when coming from search and favorites
                category?.let {
                    launch {
                        combine(
                            moviesManager.getCategoryFlow(
                                type = movieType,
                                category = category,
                                scope = viewLifecycleOwner.lifecycleScope,
                                position = position
                            ),
                            moviesManager.favoritesMap
                        ) { page, favorites ->
                            page.map {
                                BrowseMovieLoadResult.BrowseMovie(
                                    it.movie,
                                    favorites.containsKey(it.movie.id),
                                    it.position
                                )
                            }
                        }.collectLatest {
                            categoryAdapter.submitData(it)
                        }
                    }
                }
            }
        }
    }
}

data class DetailedMovie(
    val title: String,
    val voteAverage: Double,
    val genres: List<Genre>,
    val runtime: Int?,
    val overview: String,
    val topCastAndDirector: TopCastAndDirector?,
    val releaseDate: String?,
)

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any?
    ) {
        if (item is DetailedMovie) {
            val movie = item as DetailedMovie

            viewHolder.title.text = movie.title

            viewHolder.subtitle.maxLines = 6

            viewHolder.subtitle.text = "${movie.releaseDate}\n${movie.voteAverage}/10\n" +
                    "${movie.genres.joinToString(" - ") { it.name }}\n${movie.runtime?.let { "Runtime: $it minutes\n" } ?: ""}" +
                    "${movie.topCastAndDirector?.let { "Cast: ${it.cast.joinToString(", ") { it.name }}${it.director?.name?.let { "\nDirector: $it" } ?: ""}" }}"

            viewHolder.body.text = movie.overview
        }
    }
}