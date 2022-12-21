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
import com.shayo.movies.*
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import com.shayo.moviesbeforetv.tv.utils.mapToBrowseResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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

    private lateinit var backgroundViewModel: BackgroundViewModel

    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var actionAdapter: ArrayObjectAdapter
    private lateinit var watchListAction: Action
    private lateinit var detailsPresenter: FullWidthDetailsOverviewRowPresenter
    private val detailsRow = DetailsOverviewRow(Object())
    private lateinit var moreRowAdapter: androidx.leanback.paging.PagingDataAdapter<BrowseMovieLoadResult.BrowseMovieLoadSuccess>
    private lateinit var castRowAdapter: ArrayObjectAdapter

    // TODO: Create paging from favorites and then we won't have to handle it differently
    private lateinit var favoritesAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val header = HeaderItem("Browse More:")

        CardPresenter(resources.displayMetrics.widthPixels).let { cardPresenter ->
            mAdapter.add(
                ListRow(
                    HeaderItem("Full cast:"),
                    ArrayObjectAdapter(cardPresenter).also { castRowAdapter = it }
                )
            )

            mAdapter.add(
                ListRow(
                    header,
                    if (navArgs.origin == DetailsOrigin.WATCHLIST) {
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

            repeatOnLifecycle(Lifecycle.State.STARTED) {

                val position = navArgs<DetailFragmentArgs>().value.position

                // TODO: Make cache for credits so in won't fetch from network again and again
                val topCastAndDirector = creditsRepository.getCredits(movieType, movieId).fold(
                    onSuccess = {
                        it
                    },
                    onFailure = {
                        null
                    }
                )

                castRowAdapter.addAll(0, topCastAndDirector?.cast?.mapIndexed { index, credit ->
                    BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit(
                        credit, index
                    )
                })

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
                                        releaseDate
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
                        DetailsOrigin.CATEGORY -> {
                            combine(
                                moviesManager.getCategoryFlow(
                                    type = movieType,
                                    category = navArgs.queryOrCategory,
                                    scope = viewLifecycleOwner.lifecycleScope,
                                    position = position
                                ),
                                moviesManager.favoritesMap
                            ) { page, favorites ->
                                page.map {
                                    BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
                                        it.movie,
                                        favorites.containsKey(it.movie.id),
                                        it.position
                                    ) as BrowseMovieLoadResult.BrowseMovieLoadSuccess
                                }
                            }.collectLatest {
                                moreRowAdapter.submitData(it)
                            }
                        }
                        DetailsOrigin.SEARCH -> {
                            combine(
                                moviesManager.getSearchFlow(
                                    navArgs.queryOrCategory,
                                    viewLifecycleOwner.lifecycleScope,
                                    position
                                ),
                                moviesManager.favoritesMap,
                            ) { page, favorites ->
                                page.map { pagedItem ->
                                    pagedItem.mapToBrowseResult(favorites)
                                }
                            }.collectLatest {
                                moreRowAdapter.submitData(it)
                            }
                        }
                        DetailsOrigin.WATCHLIST -> {
                            moviesManager.favoritesFlow.collectLatest {
                                val data = it.mapIndexed { index, result ->
                                    result.fold(
                                        onSuccess = { movie ->
                                            BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
                                                movie,
                                                true,
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
                    }
                }
            }
        }
    }
}

enum class DetailsOrigin { CATEGORY, WATCHLIST, SEARCH }

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
            val movie = item

            viewHolder.title.text = movie.title

            viewHolder.subtitle.maxLines = 6

            viewHolder.subtitle.text = "${movie.releaseDate}\n${movie.voteAverage}/10\n" +
                    "${movie.genres.joinToString(" - ") { it.name }}\n${movie.runtime?.let { "Runtime: $it minutes\n" } ?: ""}" +
                    "${
                        movie.topCastAndDirector?.let {
                            "Cast: ${
                                it.cast.take(4).joinToString(", ") { it.name }
                            }${it.director?.name?.let { "\nDirector: $it" } ?: ""}"
                        }
                    }"

            viewHolder.body.text = movie.overview
            viewHolder.body.textScaleX = 1.1F
            viewHolder.body.setTextColor(Color.WHITE)
        }
    }
}