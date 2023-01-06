package com.shayo.moviespoint.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.shayo.movies.MovieManager
import com.shayo.movies.PagedItem
import com.shayo.moviespoint.data.query.model.Query
import com.shayo.moviespoint.data.query.repo.QueryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val QUERY_DELAY = 600L

@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val movieManager: MovieManager,
    private val queryRepository: QueryRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("") // TODO: Unite with search results
    val query = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyFlow = queryRepository.queryFlow.mapLatest { result ->
        result.fold(
            onSuccess = { queries ->
                queries.map { query ->
                    query.query
                }
            },
            onFailure = {
                null
            }
        )
    }.stateIn(
        scope = viewModelScope,
        SharingStarted.Lazily,
        initialValue = emptyList()
    )

    fun onQueryTextChange(newQuery: String) {
        _query.value = newQuery
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchFlow = _query.debounce(QUERY_DELAY).distinctUntilChanged().flatMapLatest { query ->
        if (query == "") {
            flow {
                PagingData.empty<PagedItem>()
            }
        } else {
            movieManager.getSearchFlow(query, viewModelScope, withGenres = false)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PagingData.empty()
    )

    fun watchlistClick(id: Int, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            movieManager.toggleFavorite(id, type)
        }
    }

    fun onResultClick(resultTitle: String) {
        viewModelScope.launch {
            queryRepository.add(Query(resultTitle, System.currentTimeMillis()))
        }
    }

    fun deleteHistory() {
        viewModelScope.launch {
            queryRepository.deleteAll()
        }
    }
}