package com.shayo.moviesbeforetv.tv

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.shayo.movies.Credit
import com.shayo.movies.Movie
import com.shayo.movies.MovieManager
import com.shayo.movies.UserRepository
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FAVORITES_ID = 34L

@AndroidEntryPoint
class MyBrowseFragment : BrowseSupportFragment() {
    @Inject
    lateinit var movieManager: MovieManager

    @Inject
    lateinit var userRepository: UserRepository

    private val categoriesAdapters =
        mutableListOf<PagingDataAdapter<BrowseMovieLoadResult.BrowseMovieLoadSuccess>>()

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

    private var backgroundUpdateJob: Job? = null

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

        val browseMovieLoadSuccessDiff = BrowseMovieLoadSuccessDiff()

        categories.forEach { (header, _, _) ->
            val pagingAdapter = PagingDataAdapter(cardPresenter, browseMovieLoadSuccessDiff)

            rowsAdapter.add(
                ListRow(
                    HeaderItem(header),
                    pagingAdapter
                )
            )

            categoriesAdapters.add(pagingAdapter)
        }

        settingsAdapter =
            ArrayObjectAdapter(SettingsCard(resources.displayMetrics.widthPixels))
        settingsAdapter.add(SettingsCardType.Account(null))

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
                                    FAVORITES_ID,
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
                                BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
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
                                    BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
                                        movie,
                                        true,
                                        index
                                    )
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

                            settingsAdapter.replace(
                                0,
                                SettingsCardType.Account(photoUrl?.toString())
                            )

                            loadDrawable(this@MyBrowseFragment, photoUrl?.toString(), true)?.let {
                                badgeDrawable = it
                            } ?: run {
                                // TODO: Change title, though right now it causes problems
                            }
                        } ?: run {
                            movieManager.setCollection(null)

                            settingsAdapter.replace(0, SettingsCardType.Account(null))

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
                is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie -> {
                    backgroundUpdateJob?.cancel()

                    backgroundUpdateJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(1_000)
                        if (isActive) {
                            backgroundViewModel.backgroundFlow.value = item.movie.backdropPath
                        }
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
            if (item is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie) {
                val action =
                    MyBrowseFragmentDirections.actionMyBrowseFragmentToDetailFragment(
                        item.movie.id,
                        item.movie.type,
                        item.category ?: "",
                        if (row?.id == FAVORITES_ID) DetailsOrigin.WATCHLIST else DetailsOrigin.CATEGORY,
                        item.position
                    )
                findNavController().navigate(action)
            } else if (item is SettingsCardType) {
                if (item is SettingsCardType.Account) {
                    viewLifecycleOwner.lifecycleScope.launch {

                        userRepository.getCurrentUser()?.photoUrl?.toString()?.let {

                            progressBarManager.show()

                            activityViewModels<UserImageViewModel>().value.userImage = loadDrawable(
                                this@MyBrowseFragment,
                                it
                            )

                            progressBarManager.hide()
                        }

                        findNavController().navigate(MyBrowseFragmentDirections.actionMyBrowseFragmentToLoginFragment())
                    }
                }

            }
        }
    }
}

@HiltViewModel
class UserImageViewModel @Inject constructor() : ViewModel() {
    var userImage: Drawable? = null
}

class BrowseMovieLoadSuccessDiff :
    DiffUtil.ItemCallback<BrowseMovieLoadResult.BrowseMovieLoadSuccess>() {
    override fun areItemsTheSame(
        oldItem: BrowseMovieLoadResult.BrowseMovieLoadSuccess,
        newItem: BrowseMovieLoadResult.BrowseMovieLoadSuccess
    ): Boolean {
        return if (oldItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie &&
            newItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie
        ) {
            oldItem.movie.id == newItem.movie.id
        } else if (oldItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit &&
            newItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit
        ) {
            oldItem.credit.id == newItem.credit.id
        } else {
            false
        }
    }

    override fun areContentsTheSame(
        oldItem: BrowseMovieLoadResult.BrowseMovieLoadSuccess,
        newItem: BrowseMovieLoadResult.BrowseMovieLoadSuccess
    ): Boolean {
        return if (oldItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie &&
            newItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie
        ) {
            oldItem.movie.title == newItem.movie.title &&
                    oldItem.movie.posterPath == newItem.movie.posterPath &&
                    oldItem.movie.backdropPath == newItem.movie.backdropPath &&
                    oldItem.movie.overview == newItem.movie.overview &&
                    oldItem.movie.releaseDate == newItem.movie.releaseDate &&
                    oldItem.movie.voteAverage == newItem.movie.voteAverage &&
                    oldItem.movie.genres == newItem.movie.genres &&
                    oldItem.movie.type == newItem.movie.type &&
                    oldItem.isFavorite == newItem.isFavorite
        } else if (oldItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit &&
            newItem is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit
        ) {
            oldItem.credit.name == newItem.credit.name &&
                    oldItem.credit.profilePath == newItem.credit.profilePath
        } else {
            false
        }
    }
}

sealed interface BrowseMovieLoadResult {
    sealed interface BrowseMovieLoadSuccess : BrowseMovieLoadResult {
        data class BrowseMovie(
            val movie: Movie,
            val isFavorite: Boolean = false,
            val position: Int,
            val category: String? = null,
        ) : BrowseMovieLoadSuccess

        data class BrowseCredit(
            val credit: Credit,
            val position: Int
        ) : BrowseMovieLoadSuccess
    }

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
                is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie -> {

                    cardView.titleText = item.movie.title

                    cardView.contentText =
                        "${item.movie.voteAverage}/10${if (item.movie.genres.isNotEmpty()) " - ${item.movie.genres[0].name}" else ""}"

                    if (item.isFavorite)
                        cardView.badgeImage = ContextCompat.getDrawable(
                            viewHolder.view.context,
                            R.drawable.ic_baseline_bookmark_24
                        )

                    item.movie.posterPath?.let {
                        Glide.with(viewHolder.view.context)
                            .load("https://image.tmdb.org/t/p/w500${item.movie.posterPath}") // TODO:
                            .centerCrop()
                            .error(R.drawable.ic_baseline_broken_image_24)
                            .into(cardView.mainImageView)
                    }
                        ?: cardView.mainImageView.setImageResource(R.drawable.ic_baseline_cloud_off_24)
                }
                is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit -> {
                    cardView.titleText = item.credit.name
                    cardView.contentText =
                        item.credit.knownFor.take(2).joinToString(", ") { it.title }

                    item.credit.profilePath?.let {
                        Glide.with(viewHolder.view.context)
                            .load("https://image.tmdb.org/t/p/original${item.credit.profilePath}") // TODO:
                            .centerCrop()
                            .error(R.drawable.ic_baseline_broken_image_24)
                            .into(cardView.mainImageView)
                    }
                        ?: cardView.mainImageView.setImageResource(R.drawable.ic_baseline_cloud_off_24)
                }
                is BrowseMovieLoadResult.BrowseMovieLoadError -> {
                    cardView.mainImageView.setImageResource(R.drawable.ic_baseline_broken_image_24)
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