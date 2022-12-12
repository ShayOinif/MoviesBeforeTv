package com.shayo.moviesbeforetv.tv

import android.os.Bundle
import androidx.fragment.app.activityViewModels
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val QUERY_DELAY = 300L

@AndroidEntryPoint
class MySearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    private val query = MutableStateFlow("")

    private lateinit var backgroundViewModel: BackgroundViewModel

    @Inject
    lateinit var movieManager: MovieManager

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backgroundViewModel = activityViewModels<BackgroundViewModel>().value

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
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1_000)
                    backgroundViewModel.backgroundFlow.value = item.movie.backdropPath
                }
            }
        }
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
}
