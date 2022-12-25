package com.shayo.moviespoint.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shayo.movies.MovieManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

private const val QUERY_DELAY = 300L

@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val movieManager: MovieManager,
) : ViewModel() {
    private val _query = MutableStateFlow("") // TODO: Unite with search results
    val query = _query.asStateFlow()

    fun onQueryTextChange(newQuery: String) {
        _query.value = newQuery
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchFlow = _query.debounce(QUERY_DELAY).distinctUntilChanged().flatMapLatest { query ->
        if (query == "") {
            emptyFlow()
        } else {
            movieManager.getSearchFlow(query, viewModelScope, withGenres = false)
        }
    }
}