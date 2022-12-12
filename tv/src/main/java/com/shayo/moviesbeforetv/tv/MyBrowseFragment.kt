package com.shayo.moviesbeforetv.tv

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.shayo.movies.Movie
import com.shayo.movies.MovieManager
import com.shayo.movies.UserRepository
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyBrowseFragment : BrowseSupportFragment() {
    @Inject
    lateinit var movieManager: MovieManager

    @Inject
    lateinit var userRepository: UserRepository

    private val categoriesAdapters =
        mutableListOf<PagingDataAdapter<BrowseMovieLoadResult.BrowseMovie>>()

    private lateinit var backgroundViewModel: BackgroundViewModel

    // TODO: Get categories from some where else
    private val categories = listOf(
        Triple("Popular Movies", "movie", "popular"),
        Triple("Upcoming Movies", "movie", "upcoming"),
        Triple("Popular Tv Shows", "tv", "popular"),
        Triple("Top Rated Tv Shows", "tv", "top_rated"),
    )

    private lateinit var favoritesAdapter: ArrayObjectAdapter

    private lateinit var settingsAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backgroundViewModel = activityViewModels<BackgroundViewModel>().value

        brandColor = ContextCompat.getColor(requireActivity(), R.color.brand_color)
        searchAffordanceColor = ContextCompat.getColor(requireActivity(), R.color.search_color)

        headersState = HEADERS_ENABLED

        isHeadersTransitionOnBackEnabled = true

        setOnSearchClickedListener {
            findNavController().navigate(MyBrowseFragmentDirections.actionMyBrowseFragmentToMySearchFragment())
        }

        onItemViewSelectedListener = ItemViewSelectedListener()
        onItemViewClickedListener = ItemViewClickedListener()


        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        val cardPresenter = CardPresenter(resources.displayMetrics.widthPixels)

        val movieDiff = MovieDiff()

        categories.forEach { (header, _, _) ->
            val pagingAdapter = PagingDataAdapter(cardPresenter, movieDiff)

            rowsAdapter.add(
                ListRow(
                    HeaderItem(header),
                    pagingAdapter
                )
            )

            categoriesAdapters.add(pagingAdapter)
        }

        settingsAdapter =
            ArrayObjectAdapter(GridItemPresenter(resources.displayMetrics.widthPixels))
        settingsAdapter.add("Login")

        rowsAdapter.add(ListRow(HeaderItem("Settings"), settingsAdapter))

        favoritesAdapter = ArrayObjectAdapter(cardPresenter)

        adapter = rowsAdapter

        // Todo: Unregister in proper place
        favoritesAdapter.registerObserver(
            object : ObjectAdapter.DataObserver() {
                override fun onChanged() {
                    if (favoritesAdapter.size() == 0) {

                        (adapter as ArrayObjectAdapter).removeItems(5, 1)
                    } else {
                        if (rowsAdapter.size() == 5) {

                            (adapter as ArrayObjectAdapter).add(
                                ListRow(
                                    HeaderItem("Favorites"),
                                    favoritesAdapter
                                )
                            )
                        }
                    }
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                categories.forEachIndexed { index, (_, type, category) ->
                    launch {
                        combine(
                            movieManager.getCategoryFlow(
                                type = type,
                                category = category,
                                scope = viewLifecycleOwner.lifecycleScope,
                            ),
                            movieManager.favoritesMap
                        ) { page, favorites ->
                            page.map {
                                BrowseMovieLoadResult.BrowseMovie(
                                    it.movie,
                                    favorites.containsKey(it.movie.id),
                                    it.position,
                                    category,
                                )
                            }
                        }.collectLatest {
                            categoriesAdapters[index].submitData(it)
                        }
                    }

                    /*launch {
                        categoriesAdapters[index].loadStateFlow.collectLatest { state ->

                            when (state.refresh) {
                                is LoadState.Error -> {

                                    reloadList.add {
                                        categoriesAdapters[index].refresh()
                                    }
                                }

                                else -> {

                                    reloadList.remove {
                                        categoriesAdapters[index].refresh()
                                    }
                                }
                            }

                            when (state.mediator?.append) {
                                is LoadState.Error -> {
                                    Log.d("MyTag", "append problem")
                                    reloadList.add {
                                        categoriesAdapters[index].retry()
                                    }
                                }
                                else -> {
                                    Log.d("MyTag", "append ok")
                                    reloadList.remove {
                                        categoriesAdapters[index].retry()
                                    }
                                }
                            }

                            if (state.append.endOfPaginationReached) {
                                Log.d("MyTag", "end of pagination")
                            }
                        }
                    }*/
                }

                launch {
                    movieManager.favoritesFlow.collectLatest {
                        val data = it.mapIndexed { index, result ->
                            result.fold(
                                onSuccess = { movie ->
                                    BrowseMovieLoadResult.BrowseMovie(movie, true, index)
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

                launch {
                    // TODO: Maybe find another place to handle login logout in term of favorties
                    userRepository.currentAuthUserFlow.collectLatest { currentUser ->
                        currentUser?.run {

                            movieManager.setCollection(email)

                            settingsAdapter.replace(0, "Logout")

                            loadDrawable(this@MyBrowseFragment, photoUrl?.toString())?.let {
                                badgeDrawable = it
                            } ?: run {
                                // TODO: Change title, though right now it causes problems
                            }
                        } ?: run {
                            movieManager.setCollection(null)

                            settingsAdapter.replace(0, "Login")

                            badgeDrawable = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_person_24
                            )
                        }
                    }
                }
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            when (item) {
                is BrowseMovieLoadResult.BrowseMovie -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(1_000)
                        backgroundViewModel.backgroundFlow.value = item.movie.backdropPath
                    }
                }
            }
        }
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            if (item is BrowseMovieLoadResult.BrowseMovie) {
                val action =
                    MyBrowseFragmentDirections.actionMyBrowseFragmentToDetailFragment(
                        item.movie.id,
                        item.movie.type,
                        item.category,
                        item.position
                    )
                findNavController().navigate(action)
            } else if (item is String) {
                if (item == "Login")
                    findNavController().navigate(MyBrowseFragmentDirections.actionMyBrowseFragmentToLoginFragment())
                else
                    AuthUI.getInstance()
                        .signOut(requireContext())
            }
        }
    }
}

class MovieDiff : DiffUtil.ItemCallback<BrowseMovieLoadResult.BrowseMovie>() {
    override fun areItemsTheSame(
        oldItem: BrowseMovieLoadResult.BrowseMovie,
        newItem: BrowseMovieLoadResult.BrowseMovie
    ): Boolean {
        return oldItem.movie.id == newItem.movie.id
    }

    override fun areContentsTheSame(
        oldItem: BrowseMovieLoadResult.BrowseMovie,
        newItem: BrowseMovieLoadResult.BrowseMovie
    ): Boolean {
        return oldItem.movie.title == newItem.movie.title &&
                oldItem.movie.posterPath == newItem.movie.posterPath &&
                oldItem.movie.backdropPath == newItem.movie.backdropPath &&
                oldItem.movie.overview == newItem.movie.overview &&
                oldItem.movie.releaseDate == newItem.movie.releaseDate &&
                oldItem.movie.voteAverage == newItem.movie.voteAverage &&
                oldItem.movie.genres == newItem.movie.genres &&
                oldItem.movie.type == newItem.movie.type &&
                oldItem.isFavorite == newItem.isFavorite
    }
}

sealed interface BrowseMovieLoadResult {
    data class BrowseMovie(
        val movie: Movie,
        val isFavorite: Boolean = false,
        val position: Int,
        val category: String? = null,
    ) : BrowseMovieLoadResult

    data class BrowseMovieLoadError(
        val error: Throwable
    ) : BrowseMovieLoadResult
}


class CardPresenter(width: Int) : Presenter() {

    private val width: Int = width / 6

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {

        val cardView = ImageCardView(parent?.context)

        cardView.setMainImageDimensions(width, width * 3 / 2)
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER)

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        item?.let {

            val cardView = viewHolder?.view as ImageCardView

            when (item) {
                is BrowseMovieLoadResult.BrowseMovie -> {

                    cardView.titleText = item.movie.title

                    cardView.contentText =
                        "${item.movie.voteAverage}/10${if (item.movie.genres.isNotEmpty()) " - ${item.movie.genres[0].name}" else ""}"

                    if (item.isFavorite)
                        cardView.badgeImage = ContextCompat.getDrawable(
                            viewHolder.view.context,
                            R.drawable.ic_baseline_bookmark_24
                        )

                    Glide.with(viewHolder.view.context)
                        .load("https://image.tmdb.org/t/p/w500/${item.movie.posterPath}") // TODO:
                        .centerCrop()
                        .error(R.drawable.ic_baseline_broken_image_24)
                        .into(cardView.mainImageView)
                }
                is BrowseMovieLoadResult.BrowseMovieLoadError -> {
                    Glide.with(viewHolder.view.context)
                        .load(R.drawable.ic_baseline_broken_image_24) // TODO:
                        .centerCrop()
                        .into(cardView.mainImageView)
                }
                else -> {}
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        val cardView = viewHolder?.view as ImageCardView
        // Remove references to images so that the garbage collector can free up memory
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}

private class GridItemPresenter(size: Int) : Presenter() {

    private val size = size / 6

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