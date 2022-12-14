package com.shayo.moviesbeforetv.tv

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.shayo.movies.Credit
import com.shayo.movies.Movie
import com.shayo.movies.MovieManager
import com.shayo.movies.UserRepository
import com.shayo.moviesbeforetv.tv.utils.RegularArrayAdapterDiff
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import com.shayo.moviesbeforetv.tv.utils.mapToBrowseResult
import com.shayo.moviespoint.data.usage.UsageRepository
import com.shayo.moviespoint.ui.DetailsOrigin
import com.shayo.moviespoint.viewmodels.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
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

    @Inject
    lateinit var usageRepository: UsageRepository

    private val homeViewModel by viewModels<HomeViewModel>()

    private val categoriesAdapters =
        mutableListOf<PagingDataAdapter<BrowseMovieLoadResult.BrowseMovieLoadSuccess>>()

    private lateinit var backgroundViewModel: BackgroundViewModel

    private lateinit var favoritesAdapter: ArrayObjectAdapter

    private lateinit var settingsAdapter: ArrayObjectAdapter

    private var backgroundUpdateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Home - TV")
        }

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

        homeViewModel.setup(
            withGenres = true,
            BrowseMovieLoadResult.BrowseMovieLoadSuccess::class,
        ) { media, category, _ ->
            media.mapToBrowseResult(category)
        }

        settingsAdapter =
            ArrayObjectAdapter(SettingsCard(resources.displayMetrics.widthPixels))

        settingsAdapter.add(SettingsCardType.Account(userImage = null))

        settingsAdapter.add(SettingsCardType.Usage(enabled = true))

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
                launch {
                    homeViewModel.categoriesFlows.collectLatest { categories ->
                        categories?.let {
                            val rowsAdapter = (adapter as ArrayObjectAdapter)

                            if (rowsAdapter.size() < 2) {
                                val cardPresenter =
                                    CardPresenter(resources.displayMetrics.widthPixels)

                                val browseMovieLoadSuccessDiff = BrowseMovieLoadSuccessDiff()

                                categories.forEachIndexed { index, homeCategory ->
                                    val pagingAdapter =
                                        PagingDataAdapter(cardPresenter, browseMovieLoadSuccessDiff)

                                    rowsAdapter.add(
                                        index,
                                        ListRow(
                                            HeaderItem(getString(homeCategory.nameRes)),
                                            pagingAdapter
                                        )
                                    )

                                    categoriesAdapters.add(pagingAdapter)
                                }
                            }

                            categories.forEachIndexed { index, homeCategory ->
                                launch {
                                    homeCategory.flow.collectLatest { pagedData ->

                                        categoriesAdapters[index].submitData(
                                            pagedData as PagingData<BrowseMovieLoadResult.BrowseMovieLoadSuccess> // TODO:
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                launch {
                    // TODO: Move to class
                    val diff = RegularArrayAdapterDiff()

                    movieManager.getFavoritesFlow().collectLatest {
                        val data = it.mapIndexed { index, result ->
                            result.fold(
                                onSuccess = { movie ->
                                    BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseMovie(
                                        movie,
                                        index
                                    )
                                },
                                onFailure = { error ->
                                    BrowseMovieLoadResult.BrowseMovieLoadError(error)
                                },
                            )
                        }

                        favoritesAdapter.setItems(
                            data,
                            diff
                        )
                    }
                }

                launch {
                    // TODO: Maybe find another place to handle login logout in term of favorites
                    userRepository.currentAuthUserFlow.collectLatest { currentUser ->
                        currentUser?.run {
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
                            settingsAdapter.replace(0, SettingsCardType.Account(null))

                            badgeDrawable = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_person_24
                            )
                        }
                    }
                }

                launch {
                    usageRepository.usageEnabledFlow.collectLatest { usage ->
                        settingsAdapter.replace(
                            1,
                            SettingsCardType.Usage(enabled = usage)
                        )
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
                when (item) {
                    is SettingsCardType.Account -> viewLifecycleOwner.lifecycleScope.launch {

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
                    is SettingsCardType.Usage -> usageRepository.changeUsage(!item.enabled)
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
                    oldItem.movie.isFavorite == newItem.movie.isFavorite
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


class CardPresenter(width: Int, private val person: Boolean = false) : Presenter() {

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
                        if (person) {
                            "${
                                if (item.movie.desc == null || item.movie.desc?.isEmpty() == true)
                                    viewHolder.view.context.getString(R.string.no_desc)
                                else
                                    item.movie.desc
                            }${item.movie.releaseDate?.take(4)?.let { " - $it" } ?: ""}"
                        } else {
                            "${
                                String.format(
                                    "%.0f",
                                    item.movie.voteAverage
                                )
                            }/10${if (item.movie.genres.isNotEmpty()) " - ${item.movie.genres[0].name}" else ""}${
                                item.movie.releaseDate?.take(4)?.let { " - $it" } ?: ""
                            }"
                        }

                    cardView.badgeImage = ContextCompat.getDrawable(
                        viewHolder.view.context,
                        if (item.movie.type == "movie") R.drawable.ic_baseline_local_movies_24 else R.drawable.ic_baseline_live_tv_24
                    ).apply {
                        this?.setTint(
                            if (item.movie.isFavorite) Color.WHITE else Color.GRAY
                        )
                    }

                    item.movie.posterPath?.let {
                        Glide.with(viewHolder.view.context)
                            .load("https://image.tmdb.org/t/p/w500${item.movie.posterPath}") // TODO:
                            .centerCrop()
                            .error(R.drawable.ic_baseline_broken_image_24)
                            .into(cardView.mainImageView)
                    } ?: let {
                        cardView.mainImageView.setImageResource(
                            if (item.movie.type == "movie") R.drawable.ic_baseline_local_movies_24 else R.drawable.ic_baseline_live_tv_24
                        )

                        cardView.mainImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                }
                is BrowseMovieLoadResult.BrowseMovieLoadSuccess.BrowseCredit -> {

                    cardView.badgeImage = ContextCompat.getDrawable(
                        viewHolder.view.context,
                        R.drawable.ic_baseline_person_24
                    )

                    cardView.titleText = item.credit.name
                    cardView.contentText =
                        "${
                            item.credit.knownFor.take(2).joinToString(", ") { it.title }
                        }${item.credit.description}"

                    item.credit.profilePath?.let {
                        Glide.with(viewHolder.view.context)
                            .load("https://image.tmdb.org/t/p/original${item.credit.profilePath}") // TODO:
                            .centerCrop()
                            .error(R.drawable.ic_baseline_broken_image_24)
                            .into(cardView.mainImageView)
                    } ?: let {
                        cardView.mainImageView.setImageResource(R.drawable.ic_baseline_person_24)

                        cardView.mainImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    }
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