package com.shayo.moviesbeforetv.tv

import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.map
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.shayo.movies.*
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import com.shayo.moviesbeforetv.tv.utils.mapToBrowseResult
import com.shayo.moviespoint.person.PersonRepository
import com.shayo.moviespoint.ui.DetailsOrigin.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : DetailsSupportFragment() {

    @Inject
    lateinit var moviesManager: MovieManager

    @Inject
    lateinit var videoRepository: VideoRepository

    @Inject
    lateinit var creditsRepository: CreditsRepository

    @Inject
    lateinit var personRepository: PersonRepository

    private lateinit var backgroundViewModel: BackgroundViewModel

    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var actionAdapter: ArrayObjectAdapter
    private lateinit var watchListAction: Action
    private lateinit var detailsPresenter: FullWidthDetailsOverviewRowPresenter
    private val detailsRow = DetailsOverviewRow(Object())
    private lateinit var moreRowAdapter: androidx.leanback.paging.PagingDataAdapter<BrowseMovieLoadResult.BrowseMovieLoadSuccess>
    private lateinit var castOrMoviesRowAdapter: ArrayObjectAdapter

    // TODO: Create paging from favorites and then we won't have to handle it differently
    private lateinit var favoritesAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Details - TV")
        }

        backgroundViewModel = activityViewModels<BackgroundViewModel>().value

        val navArgs by navArgs<DetailFragmentArgs>()

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

        val queryOrCategory = navArgs.queryOrCategory

        CardPresenter(resources.displayMetrics.widthPixels).let { cardPresenter ->
            mAdapter.add(
                ListRow(
                    HeaderItem("Full cast:"),
                    ArrayObjectAdapter(cardPresenter).also { castOrMoviesRowAdapter = it }
                )
            )

            if (navArgs.origin != NONE) {
                mAdapter.add(
                    ListRow(
                        HeaderItem("Browse More:"),
                        if (navArgs.origin == WATCHLIST) {
                            ArrayObjectAdapter(cardPresenter).also { favoritesAdapter = it }
                        } else {
                            androidx.leanback.paging.PagingDataAdapter(
                                cardPresenter,
                                BrowseMovieLoadSuccessDiff()
                            ).also { moreRowAdapter = it }
                        }
                    )
                )
            }
        }

        mPresenterSelector.addClassPresenter(
            ListRow::class.java,
            ListRowPresenter().apply {

                setOnItemViewClickedListener { _, item, _, _ ->
                    // TODO: Reuse with the one in the browse fragment
                    if (item is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie && item.movie.id != navArgs<DetailFragmentArgs>().value.movieId) {
                        val action =
                            DetailFragmentDirections.actionDetailFragmentSelf(
                                item.movie.id,
                                item.movie.type,
                                queryOrCategory,
                                navArgs.origin,
                                item.position
                            )
                        findNavController().navigate(action)
                    } else if (item is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit) {
                        val action =
                            DetailFragmentDirections.actionDetailFragmentToPersonFragment(personId = item.credit.id)
                        findNavController().navigate(action)
                    }
                }
            }
        )

        detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())

        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
        detailsPresenter.backgroundColor =
            ContextCompat.getColor(requireActivity(), R.color.brand_color)

        val movieId = navArgs<DetailFragmentArgs>().value.movieId
        val movieType = navArgs<DetailFragmentArgs>().value.movieType

        detailsPresenter.onActionClickedListener =
            OnActionClickedListener { action ->
                when (action.id) {

                    1L -> {
                        lifecycleScope.launch {
                            moviesManager.toggleFavorite(movieId, movieType)
                        }
                    }
                    0L -> {
                        findNavController().navigate(
                            DetailFragmentDirections.actionDetailFragmentToTrailerPlayer(
                                movieId, movieType
                            )
                        )
                    }
                }
            }


        lifecycleScope.launch {
            launch {
                videoRepository.getTrailers(movieType, movieId).onSuccess { trailers ->
                    if (trailers.isNotEmpty()) {
                        actionAdapter.add(
                            1,
                            Action(
                                0,
                                resources.getQuantityString(R.plurals.trailers, trailers.size),
                                ""
                            )
                        )
                    }
                }
            }

            // TODO: Make cache for credits so in won't fetch from network again and again
            val topCastAndDirector = creditsRepository.getCredits(movieType, movieId).fold(
                onSuccess = {
                    it
                },
                onFailure = {
                    null
                }
            )

            if (topCastAndDirector?.cast?.isEmpty() == true) {
                mAdapter.removeItems(1, 1)
            }

            castOrMoviesRowAdapter.addAll(0, topCastAndDirector?.cast?.mapIndexed { index, credit ->
                BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit(
                    credit, index
                )
            })

            repeatOnLifecycle(Lifecycle.State.STARTED) {

                val position = navArgs<DetailFragmentArgs>().value.position

                launch {
                    moviesManager.getDetailedMovieByIdFlow(movieId, movieType)
                        .collectLatest { movie ->
                            movie?.let {

                                backgroundViewModel.backgroundFlow.value = movie.backdropPath

                                detailsRow.item = with(movie) {
                                    DetailedMovie(
                                        title,
                                        voteAverage,
                                        genres,
                                        runtime,
                                        overview,
                                        topCastAndDirector,
                                        releaseDate,
                                        type
                                    )
                                }

                                detailsRow.imageDrawable = loadDrawable(
                                    this@DetailFragment,
                                    "https://image.tmdb.org/t/p/w500/${movie.posterPath}"
                                )

                                watchListAction.label1 = if (movie.isFavorite) {
                                    "Remove From Watchlist"
                                } else {
                                    "Add To Watchlist"
                                }

                                actionAdapter.replace(
                                    0, watchListAction
                                )

                                detailsRow.actionsAdapter = actionAdapter

                                adapter = mAdapter
                            }
                        }
                }

                launch {
                    when (navArgs.origin) {
                        CATEGORY -> {
                            moviesManager.getCategoryFlow(
                                type = movieType,
                                category = navArgs.queryOrCategory,
                                scope = viewLifecycleOwner.lifecycleScope,
                                position = position
                            ).collectLatest { page ->
                                moreRowAdapter.submitData(
                                    page.map { movie ->
                                        movie.mapToBrowseResult()
                                    }
                                )
                            }
                        }
                        SEARCH -> {
                            moviesManager.getSearchFlow(
                                navArgs.queryOrCategory,
                                viewLifecycleOwner.lifecycleScope,
                                position
                            ).collectLatest { page ->
                                moreRowAdapter.submitData(
                                    page.map { pagedItem ->
                                        pagedItem.mapToBrowseResult()
                                    }
                                )
                            }
                        }
                        WATCHLIST -> {
                            moviesManager.getFavoritesFlow().collectLatest {
                                val data = it.mapIndexed { index, result ->
                                    result.fold(
                                        onSuccess = { movie ->
                                            BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
                                                movie,
                                                index
                                            )
                                        },
                                        onFailure = { error ->
                                            BrowseMovieLoadResult.BrowseMovieLoadError(error)
                                        },
                                    )
                                }

                                favoritesAdapter.clear()

                                data.forEach { loadResult ->
                                    favoritesAdapter.add(loadResult)
                                }
                            }
                        }
                        else -> {}
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
    val type: String,
)

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any?
    ) {
        if (item is DetailedMovie) {
            viewHolder.title.text =
                with(viewHolder.view.context) {
                    getString(
                        R.string.title,
                        item.title,
                        getString(
                            if (item.type == "movie")
                                R.string.movie
                            else
                                R.string.show
                        )
                    )
                }

            viewHolder.subtitle.maxLines = 6

            viewHolder.subtitle.text = buildString {
                append("${item.releaseDate}\n${"%.1f".format(item.voteAverage)} / 10\n")
                append("${item.genres.joinToString(" - ") { it.name }}\n${item.runtime?.let { "Runtime: $it minutes\n" } ?: ""}")
                append(
                    item.topCastAndDirector?.let {
                        buildString {
                            append("Cast: ")
                            append(it.cast.take(4).joinToString(", ") { credit -> credit.name })
                            append(it.director?.name?.let { director -> "\nDirector: $director" }
                                ?: "")
                        }
                    }
                )
            }

            viewHolder.body.text = item.overview
            viewHolder.body.textScaleX = 1.1F
            viewHolder.body.setTextColor(Color.WHITE)
        }
    }
}