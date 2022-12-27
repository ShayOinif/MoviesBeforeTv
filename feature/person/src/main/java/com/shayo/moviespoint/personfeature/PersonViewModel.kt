package com.shayo.moviespoint.personfeature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shayo.movies.MovieManager
import com.shayo.moviespoint.person.CombinedCredits
import com.shayo.moviespoint.person.Person
import com.shayo.moviespoint.person.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PersonViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val movieManager: MovieManager,
) : ViewModel() {

    private val parsonIdFlow = MutableStateFlow<Int?>(null)

    fun setPersonId(personId: Int?) {
        parsonIdFlow.value = personId
    }

    private fun retry(personId: Int) {
        setPersonId(null)
        setPersonId(personId)
    }

    // TODO: Handle favorites!!!

    @OptIn(ExperimentalCoroutinesApi::class)
    var personFlow = combine(
        parsonIdFlow, movieManager.favoritesMap
    ) { personId, favoritesMap ->
        personId?.let {
            personRepository.getBio(personId).fold(
                onSuccess = { person ->
                    PersonUiState.Success(
                        person.copy(
                            combinedCredits = CombinedCredits(
                                cast = person.combinedCredits.cast.map { media ->
                                    media.copy(isFavorite = favoritesMap.containsKey(media.id))
                                },
                                crew = person.combinedCredits.crew.map { media ->
                                    media.copy(isFavorite = favoritesMap.containsKey(media.id))
                                },
                            )
                        )
                    )
                },
                onFailure = { error ->
                    PersonUiState.Error(
                        retry = {
                            retry(personId)
                        },
                        message = error.message
                    )
                }
            )
        } ?: PersonUiState.Loading
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(1_500), // TODO:
        initialValue = PersonUiState.Loading,
    )

    fun watchlistClick(id: Int, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            movieManager.toggleFavorite(id, type)
        }
    }
}

internal sealed interface PersonUiState {
    data class Success(
        val person: Person
    ) : PersonUiState

    data class Error(
        val retry: () -> Unit,
        val message: String? = null,
    ) : PersonUiState

    object Loading : PersonUiState
}