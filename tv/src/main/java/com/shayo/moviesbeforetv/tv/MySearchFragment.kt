package com.shayo.moviesbeforetv.tv

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.shayo.movies.MovieManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val QUERY_DELAY = 300L

@AndroidEntryPoint
class MySearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    private val query = MutableStateFlow("")

    private lateinit var mBackgroundManager: BackgroundManager
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null
    private val mHandler = Handler(Looper.myLooper()!!)

    @Inject
    lateinit var movieManager: MovieManager

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)

        setSearchResultProvider(this)

        val cardPresenter = CardPresenter(mMetrics.widthPixels)

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
                query.debounce(QUERY_DELAY).distinctUntilChanged().collectLatest { query ->
                    if (query == "") {
                        pagingAdapter.submitData(PagingData.empty())
                    } else {
                        combine(
                            movieManager.getSearchFlow(query, lifecycleScope),
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

        setOnItemViewSelectedListener { _, item, _, _ ->
            if (item is BrowseMovie) {
                mBackgroundUri = item.movie.backdropPath
                startBackgroundTimer()
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

    // TODO: Also in browse, unite
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

    private fun startBackgroundTimer() {
        mBackgroundTimer?.cancel()
        mBackgroundTimer = Timer()
        mBackgroundTimer?.schedule(UpdateBackgroundTask(), 1000)
    }

    private inner class UpdateBackgroundTask : TimerTask() {

        override fun run() {
            mHandler.post { updateBackground(mBackgroundUri) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mBackgroundTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()

        updateBackground(mBackgroundUri)
    }
}
