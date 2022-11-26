package com.shayo.home

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val homeViewModel: HomeViewModel by viewModels()

        val recyclerView = root.findViewById<RecyclerView>(R.id.movies)
        val errorText = root.findViewById<TextView>(R.id.error_message)
        val refresh = root.findViewById<Button>(R.id.retry_button)
        val progress = root.findViewById<CircularProgressIndicator>(R.id.loading)

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                homeViewModel.homeUiState.flowWithLifecycle(viewLifecycleOwner.lifecycle).collect {
                    recyclerView.isVisible = it is HomeUiState.Success
                    errorText.isVisible = it is HomeUiState.Error
                    refresh.isVisible = it is HomeUiState.Error
                    progress.isVisible = it is HomeUiState.Loading

                    if (it is HomeUiState.Success) {
                        recyclerView.adapter = it.adapter
                    } else if (it is HomeUiState.Error) {
                        errorText.text = it.message
                    }
                }
            }

            launch {
                keysFlow.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED).collect {
                    if (it == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        recyclerView.fling(1000, 0)
                    }
                }
            }
        }

        refresh.setOnClickListener {
            homeViewModel.refresh()
        }

        return root
    }

    companion object {
        val keysFlow = MutableStateFlow<Int?>(null)
    }
}