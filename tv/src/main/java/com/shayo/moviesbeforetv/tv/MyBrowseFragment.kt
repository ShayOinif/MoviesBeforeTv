package com.shayo.moviesbeforetv.tv

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.leanback.widget.ObjectAdapter.DataObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.shayo.movies.Movie
import com.shayo.movies.MovieManager
import com.shayo.movies.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyBrowseFragment : BrowseSupportFragment() {
    @Inject
    lateinit var movieManager: MovieManager

    @Inject
    lateinit var userRepository: UserRepository

    override fun onResume() {
        super.onResume()

        updateBackground(mBackgroundUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(requireActivity().window)
        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)

        brandColor = ContextCompat.getColor(requireActivity(), R.color.brand_color)
        searchAffordanceColor = ContextCompat.getColor(requireActivity(), R.color.search_color)

        headersState = HEADERS_ENABLED

        isHeadersTransitionOnBackEnabled = true

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter(mMetrics.widthPixels)

        val movieDiff = MovieDiff()

        // TODO:
        listOf(
            Triple("Popular Movies", "movie","popular"),
            Triple("Upcoming Movies","movie", "upcoming"),
            Triple("Popular Tv Shows","tv", "popular"),
            Triple("Top Rated Tv Shows","tv", "top_rated"),
        ).forEach { (header, type, category) ->
            val pagingAdapter = PagingDataAdapter(cardPresenter, movieDiff)

            rowsAdapter.add(
                ListRow(
                    HeaderItem(header),
                    pagingAdapter
                )
            )

            lifecycleScope.launch {
                combine(
                    movieManager.getCategoryFlow(type = type, category = category, scope = lifecycleScope),
                    movieManager.favoritesMap
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

        val gridHeader = HeaderItem( "Settings")

        val mGridPresenter = GridItemPresenter(mMetrics.widthPixels / 6)
        val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
        gridRowAdapter.add("Login")

        rowsAdapter.add(ListRow(gridHeader, gridRowAdapter))


        val pagingAdapter3 = ArrayObjectAdapter(cardPresenter)

        val header3 = HeaderItem("Favorites")

        adapter = rowsAdapter

        pagingAdapter3.registerObserver(
            object : DataObserver() {
                override fun onChanged() {
                    if (pagingAdapter3.size() == 0) {
                        rowsAdapter.removeItems(5, 1)

                        adapter = rowsAdapter
                    } else {
                        if (rowsAdapter.size() == 5) {
                            rowsAdapter.add(ListRow(header3, pagingAdapter3))

                            adapter = rowsAdapter
                        }
                    }
                }
            }
        )

        lifecycleScope.launch {
            launch {
                movieManager.getFavoritesFlow().collectLatest {
                    val data = it.map {
                        BrowseMovie(it, true)
                    }

                    pagingAdapter3.clear()

                    data.forEach {
                        pagingAdapter3.add(it)
                    }
                }
            }
        }

        /*pagingAdapter2.addLoadStateListener { state ->
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
        }*/

        lifecycleScope.launch {
            // TODO: Maybe find another place to handle login logout in term of favorties
            userRepository.currentAuthUserFlow.collectLatest { currentUser ->
                currentUser?.run {
                    movieManager.setCollection(email)

                    gridRowAdapter.replace(0, "Logout")


                    photoUrl?.let { photo ->
                        badgeDrawable = drawableFromUrl(photo.toString())
                    } ?: run { title = displayName }
                } ?: run {
                    movieManager.setCollection(null)

                    gridRowAdapter.replace(0, "Login")

                    badgeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_person_24)
                }
            }
        }

        setOnSearchClickedListener {
            findNavController().navigate(MyBrowseFragmentDirections.actionMyBrowseFragmentToMySearchFragment())
        }

        onItemViewSelectedListener = ItemViewSelectedListener()
        onItemViewClickedListener = ItemViewClickedListener()
    }

    private suspend fun drawableFromUrl(url: String): Drawable {
        return withContext(Dispatchers.IO) {
            val x: Bitmap
            val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val input: InputStream = connection.inputStream
            x = BitmapFactory.decodeStream(input)
            BitmapDrawable(Resources.getSystem(), x)
        }
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
            if (item is BrowseMovie) {
                val action =
                    MyBrowseFragmentDirections.actionMyBrowseFragmentToDetailFragment(item.movie)
                findNavController().navigate(action)
            } else if (item is String) {
                if (item == "Login")
                    findNavController().navigate(MyBrowseFragmentDirections.actionMyBrowseFragmentToLoginFragment())
                else
                    userRepository.signOut()
            }
        }
    }
}

private class MovieDiff : DiffUtil.ItemCallback<BrowseMovie>() {
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
}

data class BrowseMovie(
    val movie: Movie,
    val isFavorite: Boolean = false,
)

class CardPresenter(width: Int) : Presenter() {

    private val width: Int = width / 6

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
            cardView.setMainImageDimensions(width, width * 3 / 2)
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

private class GridItemPresenter(private val size: Int) : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = TextView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(size, size)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.setBackgroundColor(Color.BLUE)
        view.setTextColor(Color.WHITE)
        view.gravity = Gravity.CENTER
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        (viewHolder.view as TextView).text = item as String
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}