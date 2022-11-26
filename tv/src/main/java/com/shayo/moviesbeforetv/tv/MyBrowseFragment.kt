package com.shayo.moviesbeforetv.tv

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.app.ErrorSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.shayo.movies.Movie
import com.shayo.movies.MoviesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyBrowseFragment : BrowseSupportFragment() {

    @Inject
    lateinit var moviesRepository: MoviesRepository

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

        // set search icon color // TODO
        //searchAffordanceColor = Color.WHITE

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()


        val pagingAdapter: PagingDataAdapter<Movie> = PagingDataAdapter(cardPresenter,
            object : DiffUtil.ItemCallback<Movie>() {
                override fun areItemsTheSame(
                    oldItem: Movie,
                    newItem: Movie
                ): Boolean {
                    return oldItem.id === newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: Movie,
                    newItem: Movie
                ): Boolean {
                    return oldItem.title == newItem.title &&
                            oldItem.posterPath == newItem.posterPath &&
                            oldItem.backdropPath == newItem.backdropPath &&
                            oldItem.overview == newItem.overview &&
                            oldItem.releaseDate == newItem.releaseDate
                }
            })


        val header = HeaderItem("Popular Movies")

        rowsAdapter.add(ListRow(header, pagingAdapter))



        val pagingAdapter2: PagingDataAdapter<Movie> = PagingDataAdapter(cardPresenter,
            object : DiffUtil.ItemCallback<Movie>() {
                override fun areItemsTheSame(
                    oldItem: Movie,
                    newItem: Movie
                ): Boolean {
                    return oldItem.id === newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: Movie,
                    newItem: Movie
                ): Boolean {
                    return oldItem.title == newItem.title &&
                            oldItem.posterPath == newItem.posterPath &&
                            oldItem.backdropPath == newItem.backdropPath &&
                            oldItem.overview == newItem.overview &&
                            oldItem.releaseDate == newItem.releaseDate
                }
            })


        val header2 = HeaderItem("Upcoming Movies")

        rowsAdapter.add(ListRow(header2, pagingAdapter2))

        adapter = rowsAdapter


        val pager = moviesRepository.getMoviesPager("popular")
        val pager2 = moviesRepository.getMoviesPager("upcoming")


        lifecycleScope.launch {
            launch {
                pager.flow.collectLatest {
                    pagingAdapter.submitData(it)
                }
            }

            launch {
                pager2.flow.collectLatest {
                    pagingAdapter2.submitData(it)
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

        /*lifecycleScope.launch {
            val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
            val cardPresenter = CardPresenter()

            val pagingAdapter: PagingDataAdapter<Movie> = PagingDataAdapter(cardPresenter,
                object : DiffUtil.ItemCallback<Movie>() {
                    override fun areItemsTheSame(
                        oldItem: Movie,
                        newItem: Movie
                    ): Boolean {
                        return oldItem.id === newItem.id
                    }

                    override fun areContentsTheSame(
                        oldItem: Movie,
                        newItem: Movie
                    ): Boolean {
                        return oldItem.title == newItem.title &&
                                oldItem.posterPath == newItem.posterPath &&
                                oldItem.backdropPath == newItem.backdropPath &&
                                oldItem.overview == newItem.overview &&
                                oldItem.releaseDate == newItem.releaseDate
                    }
                })


            val header = HeaderItem("Popular Movies")

            rowsAdapter.add(ListRow(header, pagingAdapter))

            launch {
                pager.flow.collectLatest {
                    pagingAdapter.submitData(it)
                }
            }

            val listRowAdapter2 = ArrayObjectAdapter(cardPresenter)

            val movies2 = moviesRepository.getMovies("upcoming").getOrNull()!!

            listRowAdapter2.addAll(0, movies2)

            val header2 = HeaderItem("Upcoming Movies")

            rowsAdapter.add(ListRow(header2, listRowAdapter2))



            adapter = rowsAdapter
        }*/

        setOnSearchClickedListener {
            Toast.makeText(requireContext(), "Implement your own in-app search", Toast.LENGTH_LONG)
                .show()
        }

        onItemViewSelectedListener = ItemViewSelectedListener()
        onItemViewClickedListener = ItemViewClickedListener()
    }


    private fun updateBackground(uri: String?) {
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide.with(requireActivity())
            .load("https://image.tmdb.org/t/p/original/$uri")
            .centerCrop()
            .error(mDefaultBackground)
            .into<SimpleTarget<Drawable>>(
                object : SimpleTarget<Drawable>(width, height) {
                    override fun onResourceReady(
                        drawable: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        mBackgroundManager.drawable = drawable
                    }
                })
        mBackgroundTimer?.cancel()
    }

    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null
    private val mHandler = Handler(Looper.myLooper()!!)

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            if (item is Movie) {
                mBackgroundUri = item.backdropPath
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
                MyBrowseFragmentDirections.actionMyBrowseFragmentToDetailFragment(item as Movie)
            findNavController().navigate(action)
        }
    }
}

class CardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        /*sDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
        sSelectedBackgroundColor =
            ContextCompat.getColor(parent.context, R.color.selected_background)
        mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.movie)*/

        val cardView = object : ImageCardView(parent?.context) {
            override fun setSelected(selected: Boolean) {
                // updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        //updateCardBackgroundColor(cardView, false)
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        /*val movie = item as Movie
        val cardView = viewHolder.view as ImageCardView

        if (movie.cardImageUrl != null) {
            cardView.titleText = movie.title
            cardView.contentText = movie.studio
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            Glide.with(viewHolder.view.context)
                .load(movie.cardImageUrl)
                .centerCrop()
                .error(mDefaultCardImage)
                .into(cardView.mainImageView)
        }*/

        item?.let {
            val movie = item as Movie
            val cardView = viewHolder?.view as ImageCardView

            cardView.titleText = movie.title
            cardView.setMainImageDimensions(200, 300)

            cardView.contentText = "Some short description, bring it from the api"

            cardView.badgeImage = ContextCompat.getDrawable(
                viewHolder.view.context,
                R.drawable.ic_baseline_bookmark_border_24
            )


            Glide.with(viewHolder.view.context)
                .load("https://image.tmdb.org/t/p/w500/${movie.posterPath}")
                .centerCrop()
                //.error(mDefaultCardImage)
                .into(cardView.mainImageView)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        /*Log.d(TAG, "onUnbindViewHolder")
        val cardView = viewHolder.view as ImageCardView
        // Remove references to images so that the garbage collector can free up memory
        cardView.badgeImage = null
        cardView.mainImage = null*/
    }

}