package com.shayo.moviespoint.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shayo.movies.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class AccountViewModel @Inject constructor(
    userRepository: UserRepository,
) : ViewModel() {
    val userFlow = userRepository.currentAuthUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(1_500), // TODO:
            initialValue = null,
        )
}