package com.shayo.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shayo.movies.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository,
) : ViewModel() {
    val homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    init {
        load()
    }

    fun refresh() {
        if (homeUiState.value is HomeUiState.Error)
            load()
    }

    private fun load() {
        homeUiState.value  = HomeUiState.Loading

        viewModelScope.launch {
            moviesRepository.getMovies("popular")
                .onSuccess {
                    homeUiState.value = HomeUiState.Success(MoviesAdapter().apply {
                        this.submitList(it)
                    })
                }.onFailure {
                    homeUiState.value = HomeUiState.Error(it.message)
                }
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val adapter: MoviesAdapter
    ) : HomeUiState

    data class Error(
        val message: String?
    ) : HomeUiState
}