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
import com.shayo.movies.FavoritesRepository
import com.shayo.movies.GenreRepository
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import com.shayo.moviespoint.person.Person
import com.shayo.moviespoint.person.PersonRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class PersonFragment : DetailsSupportFragment() {

    @Inject
    lateinit var personRepository: PersonRepository

    @Inject
    lateinit var favoritesRepository: FavoritesRepository

    @Inject
    lateinit var genreRepository: GenreRepository

    private lateinit var backgroundViewModel: BackgroundViewModel

    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var detailsPresenter: FullWidthDetailsOverviewRowPresenter
    private val detailsRow = DetailsOverviewRow(Object())
    private lateinit var moviesRowAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backgroundViewModel = activityViewModels<BackgroundViewModel>().value

        val navArgs by navArgs<PersonFragmentArgs>()

        val mPresenterSelector = ClassPresenterSelector()
        mAdapter = ArrayObjectAdapter(mPresenterSelector)

        mAdapter.add(detailsRow)

        CardPresenter(resources.displayMetrics.widthPixels).let { cardPresenter ->
            mAdapter.add(
                ListRow(
                    HeaderItem("All Movies:"),
                    ArrayObjectAdapter(cardPresenter).also { moviesRowAdapter = it }
                )
            )
        }

        mPresenterSelector.addClassPresenter(
            ListRow::class.java,
            ListRowPresenter().apply {

                setOnItemViewClickedListener { _, item, _, _ ->
                    if (item is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie) {
                        val action =
                            PersonFragmentDirections.actionPersonFragmentToDetailFragment(
                                item.movie.id,
                                item.movie.type,
                                "",
                                DetailsOrigin.NONE,
                                item.position
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        )

        detailsPresenter = FullWidthDetailsOverviewRowPresenter(PersonDescriptionPresenter())

        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
        detailsPresenter.backgroundColor =
            ContextCompat.getColor(requireActivity(), R.color.brand_color)

        adapter = mAdapter

        lifecycleScope.launch {
            personRepository.getBio(navArgs.personId).map {

                launch(Dispatchers.Default) {
                    backgroundViewModel.backgroundFlow.value =
                        it.combinedCredits.cast.maxByOrNull { movie ->
                            movie.popularity ?: 0.0
                        }?.backdropPath
                }

                detailsRow.item = it

                it.profilePath?.run {
                    detailsRow.imageDrawable = loadDrawable(
                        this@PersonFragment,
                        "https://image.tmdb.org/t/p/original$this"
                    )
                }

                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    combine(
                        favoritesRepository.favoritesMap,
                        genreRepository.movieGenresFlow
                    ) { favoritesMap, genreMap ->
                        Pair(favoritesMap, genreMap)
                    }.collectLatest { (favoritesMap, genreMap) ->
                        withContext(Dispatchers.Default) {
                            moviesRowAdapter.clear()

                            moviesRowAdapter.addAll(
                                0,
                                it.combinedCredits.cast.sortedByDescending { movie ->
                                    movie.releaseDate
                                }.mapIndexed { index, movie ->
                                    // TODO: Make a common function for mapping favs and genres
                                    BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
                                        movie = movie.copy(
                                            genres = movie.genres.map { genre ->
                                                genreMap[genre.id] ?: genre
                                            },
                                            isFavorite = favoritesMap.containsKey(movie.id)
                                        ),
                                        position = index,
                                    )
                                })

                            moviesRowAdapter.notifyArrayItemRangeChanged(
                                0,
                                it.combinedCredits.cast.size
                            )
                        }
                    }
                }
            }
        }
    }
}

class PersonDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any?
    ) {
        if (item is Person) {
            viewHolder.title.text = item.name

            viewHolder.body.text = item.biography
            viewHolder.body.textScaleX = 1.1F
            viewHolder.body.setTextColor(Color.WHITE)
        }
    }
}