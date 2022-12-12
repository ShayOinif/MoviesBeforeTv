package com.shayo.moviesbeforetv.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.map
import com.shayo.movies.MovieManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val QUERY_DELAY = 300L

@AndroidEntryPoint
class MySearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider, FragmentWithBackground {
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    private val query = MutableStateFlow("")

    @Inject
    lateinit var movieManager: MovieManager

    override lateinit var backgroundManager: BackgroundManager

    override var backgroundFlow = MutableStateFlow<Background?>(null)

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSearchResultProvider(this)

        val cardPresenter = CardPresenter(resources.displayMetrics.widthPixels)

        val pagingAdapter: PagingDataAdapter<BrowseMovieLoadResult.BrowseMovie> = PagingDataAdapter(cardPresenter,
            MovieDiff())

        val header = HeaderItem("Search Results:")

        rowsAdapter.add(ListRow(header, pagingAdapter))

        lifecycleScope.launch {
            launch {
                query.debounce(QUERY_DELAY).distinctUntilChanged().collectLatest { query ->
                    if (query == "") {
                        pagingAdapter.submitData(PagingData.empty())
                    } else {
                        combine(
                            movieManager.getSearchFlow(query, lifecycleScope),
                            movieManager.favoritesMap,
                        ) { page, favorites ->
                            page.map {
                                BrowseMovieLoadResult.BrowseMovie(
                                    it,
                                    favorites.containsKey(it.id),
                                    0, // TODO:
                                )
                            }
                        }.collectLatest {
                            pagingAdapter.submitData(it)
                        }
                    }
                }
            }
        }

        setOnItemViewClickedListener { _, item, _, _ ->

            with (item as BrowseMovieLoadResult.BrowseMovie) {
                findNavController().navigate(MySearchFragmentDirections.actionMySearchFragmentToDetailFragment(movie.id, movie.type, null))
            }
        }

        setOnItemViewSelectedListener { _, item, _, _ ->
            if (item is BrowseMovieLoadResult.BrowseMovie) {
                backgroundFlow.value = item.movie.backdropPath?.let {
                    Background.HasBackground(it)
                } ?: Background.NoBackground
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackgroundUpdate(viewLifecycleOwner, this, requireActivity())
    }

    override fun getResultsAdapter(): ObjectAdapter {
        return rowsAdapter
    }

    override fun onQueryTextChange(newQuery: String): Boolean {
        query.value = newQuery

        return true
    }

    override fun onQueryTextSubmit(newQuery: String): Boolean {
        query.value = newQuery

        return true
    }

    override fun onResume() {
        super.onResume()

        updateNow(this)
    }
}
