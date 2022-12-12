package com.shayo.moviesbeforetv.tv

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class BackgroundViewModel @Inject constructor() : ViewModel() {
    var backgroundFlow = MutableStateFlow<String?>(null)
}