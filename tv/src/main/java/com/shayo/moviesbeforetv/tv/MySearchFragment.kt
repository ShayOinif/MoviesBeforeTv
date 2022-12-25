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
import com.shayo.moviesbeforetv.tv.utils.mapToBrowseResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    private var backgroundUpdateJob: Job? = null

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backgroundViewModel = activityViewModels<BackgroundViewModel>().value

        setSearchResultProvider(this)

        val cardPresenter = CardPresenter(resources.displayMetrics.widthPixels)

        val pagingAdapter: PagingDataAdapter<BrowseMovieLoadResult.BrowseMovieLoadSuccess> =
            PagingDataAdapter(
                cardPresenter,
                BrowseMovieLoadSuccessDiff()
            )

        val header = HeaderItem("Search Results:")

        rowsAdapter.add(ListRow(header, pagingAdapter))

        lifecycleScope.launch {
            launch {
                query.debounce(QUERY_DELAY).distinctUntilChanged().collectLatest { query ->
                    if (query == "") {
                        pagingAdapter.submitData(PagingData.empty())
                    } else {
                            movieManager.getSearchFlow(query, lifecycleScope).collectLatest { page ->
                            pagingAdapter.submitData(
                                page.map { pagedItem ->
                                    pagedItem.mapToBrowseResult()
                                }
                            )
                        }
                    }
                }
            }
        }

        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie) {
                findNavController().navigate(
                    MySearchFragmentDirections.actionMySearchFragmentToDetailFragment(
                        item.movie.id,
                        item.movie.type,
                        query.value,
                        DetailsOrigin.SEARCH,
                        item.position
                    )
                )
            } else if (item is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit) {
                val action =
                    MySearchFragmentDirections.actionMySearchFragmentToPersonFragment(personId = item.credit.id)
                findNavController().navigate(action)
            }
        }

        setOnItemViewSelectedListener { _, item, _, _ ->
            if (item is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie) {
                backgroundUpdateJob?.cancel()

                backgroundUpdateJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(1_000)
                    if (isActive) {
                        backgroundViewModel.backgroundFlow.value = item.movie.backdropPath
                    }
                }
            }

            if (item is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit) {
                backgroundUpdateJob?.cancel()

                backgroundUpdateJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(1_000)
                    if (isActive) {
                        backgroundViewModel.backgroundFlow.value = item.credit.knownFor.firstOrNull()?.backdropPath
                    }
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
