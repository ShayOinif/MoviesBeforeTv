package com.shayo.moviesbeforetv.tv

import android.os.Bundle
import android.util.Log
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import com.shayo.movies.MovieManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MySearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    private val query = MutableStateFlow("")

    @Inject
    lateinit var movieManager: MovieManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)


        val cardPresenter = CardPresenter()

        val pagingAdapter: PagingDataAdapter<BrowseMovie> = PagingDataAdapter(cardPresenter,
            object : DiffUtil.ItemCallback<BrowseMovie>() {
                override fun areItemsTheSame(
                    oldItem: BrowseMovie,
                    newItem: BrowseMovie
                ): Boolean {
                    return oldItem.movie.id === newItem.movie.id
                }

                override fun areContentsTheSame(
                    oldItem: BrowseMovie,
                    newItem: BrowseMovie
                ): Boolean {
                    return oldItem.movie.title == newItem.movie.title &&
                            oldItem.movie.posterPath == newItem.movie.posterPath &&
                            oldItem.movie.backdropPath == newItem.movie.backdropPath &&
                            oldItem.movie.overview == newItem.movie.overview &&
                            oldItem.movie.releaseDate == newItem.movie.releaseDate &&
                            oldItem.isFavorite == newItem.isFavorite
                }
            })

        val header = HeaderItem("Search Results:")

        rowsAdapter.add(ListRow(header, pagingAdapter))

        lifecycleScope.launch {
            launch {
                query.collectLatest { query ->
                    if (query == "") {
                        Log.d("MyTag", "Empty")
                        pagingAdapter.submitData(PagingData.empty())
                    } else {
                        combine(
                            movieManager.getSearchWithGenrePager(query).flow.cachedIn(lifecycleScope),
                            movieManager.favoritesMap,
                        ) { page, favorites ->
                            page.map {
                                BrowseMovie(
                                    it,
                                    favorites.containsKey(it.id)
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
            findNavController().navigate(MySearchFragmentDirections.actionMySearchFragmentToDetailFragment((item as BrowseMovie).movie))
        }
    }

    override fun getResultsAdapter(): ObjectAdapter {
        return rowsAdapter
    }

    override fun onQueryTextChange(newQuery: String): Boolean {
        query.value = newQuery

        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return true
    }

    companion object {
        private val SEARCH_DELAY_MS = 300
    }
}
