package com.shayo.moviesbeforetv.tv

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.app.ErrorSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.leanback.widget.ObjectAdapter.DataObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.shayo.movies.Movie
import com.shayo.movies.MovieManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyBrowseFragment : BrowseSupportFragment() {

    @Inject
    lateinit var movieManager: MovieManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(requireActivity().window)
        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)

        title = "My Movies!!!"

        badgeDrawable = ContextCompat.getDrawable(requireContext(), R.mipmap.ic_banner)

        brandColor = ContextCompat.getColor(requireActivity(), R.color.brand_color)

        headersState = HEADERS_ENABLED

        isHeadersTransitionOnBackEnabled = true

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
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


        val header = HeaderItem("Popular Movies")

        rowsAdapter.add(ListRow(header, pagingAdapter))

        val pagingAdapter2: PagingDataAdapter<BrowseMovie> = PagingDataAdapter(cardPresenter,
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


        val pagingAdapter3: PagingDataAdapter<BrowseMovie> = PagingDataAdapter(cardPresenter,
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


        val header2 = HeaderItem("Upcoming Movies")

        val header3 = HeaderItem("Favorites")

        rowsAdapter.add(ListRow(header2, pagingAdapter2))

        //rowsAdapter.add(ListRow(header3, pagingAdapter3))

        adapter = rowsAdapter


        val pager = movieManager.getMoviesWithGenrePager("popular")
        val pager2 = movieManager.getMoviesWithGenrePager("upcoming")
        val favorites = movieManager.getFavoritesPager().cachedIn(lifecycleScope)

        pagingAdapter3.registerObserver(
            object : DataObserver() {
                override fun onChanged() {
                    if (pagingAdapter3.size() == 0) {
                        rowsAdapter.removeItems(2, 1)

                        adapter = rowsAdapter
                    } else {
                        if (rowsAdapter.size() == 2) {
                            rowsAdapter.add(ListRow(header3, pagingAdapter3))

                            adapter = rowsAdapter
                        }
                    }
                }
            }
        )

        lifecycleScope.launch {
            launch {
                combine(pager.flow.cachedIn(lifecycleScope), movieManager.favoritesMap) { page, favorites ->
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

            launch {
                combine(pager2.flow.cachedIn(lifecycleScope), movieManager.favoritesMap) { page, favorites ->
                    page.map {
                        BrowseMovie(
                            it,
                            favorites.containsKey(it.id)
                        )
                    }
                }.collectLatest {
                    pagingAdapter2.submitData(it)
                }
            }

            launch {
                favorites.collectLatest {

                    val data = it.map {

                        BrowseMovie(it, true)
                    }

                    pagingAdapter3.submitData(data)
                }
            }
        }

        pagingAdapter2.addLoadStateListener { state ->
            if (state.refresh is LoadState.Error) {
                val mErrorFragment = ErrorSupportFragment()

                parentFragmentManager
                    .beginTransaction()
                    .add(R.id.nav_host_fragment, mErrorFragment)
                    .commit()

                mErrorFragment.title = (state.refresh as LoadState.Error).error.message
                mErrorFragment.buttonText = "Tap To Retry"
                mErrorFragment.setButtonClickListener {
                    parentFragmentManager
                        .beginTransaction()
                        .remove(mErrorFragment)
                        .commit()

                    pagingAdapter2.refresh()
                }
            }
        }

        setOnSearchClickedListener {
            findNavController().navigate(MyBrowseFragmentDirections.actionMyBrowseFragmentToMySearchFragment())
        }

        onItemViewSelectedListener = ItemViewSelectedListener()
        onItemViewClickedListener = ItemViewClickedListener()
    }


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

    private lateinit var mBackgroundManager: BackgroundManager
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null
    private val mHandler = Handler(Looper.myLooper()!!)

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            if (item is BrowseMovie) {
                mBackgroundUri = item.movie.backdropPath
                startBackgroundTimer()
            }
        }
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

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            val action =
                MyBrowseFragmentDirections.actionMyBrowseFragmentToDetailFragment((item as BrowseMovie).movie)
            findNavController().navigate(action)
        }
    }
}

data class BrowseMovie(
    val movie: Movie,
    val isFavorite: Boolean = false,
)

class CardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {

        val cardView = ImageCardView(parent?.context)

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {

        item?.let {
            val browseMovie = item as BrowseMovie
            val cardView = viewHolder?.view as ImageCardView

            cardView.titleText = browseMovie.movie.title
            cardView.setMainImageDimensions(200, 300) // TODO:
            cardView.setMainImageScaleType(ImageView.ScaleType.CENTER)

            cardView.contentText =
                "${browseMovie.movie.voteAverage}/10${if (browseMovie.movie.genres.isNotEmpty()) " - ${browseMovie.movie.genres[0].name}" else ""}"

            if (browseMovie.isFavorite)
                cardView.badgeImage = ContextCompat.getDrawable(
                    viewHolder.view.context,
                    R.drawable.ic_baseline_bookmark_24
                )

            Glide.with(viewHolder.view.context)
                .load("https://image.tmdb.org/t/p/w500/${browseMovie.movie.posterPath}") // TODO:
                .centerCrop()
                .error(R.drawable.ic_baseline_broken_image_24)
                .into(cardView.mainImageView)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        val cardView = viewHolder?.view as ImageCardView
        // Remove references to images so that the garbage collector can free up memory
        cardView.badgeImage = null
        cardView.mainImage = null
    }

}