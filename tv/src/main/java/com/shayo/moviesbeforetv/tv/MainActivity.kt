package com.shayo.moviesbeforetv.tv

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BackgroundManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val backgroundManager = BackgroundManager.getInstance(this)

        backgroundManager.attach(window)

        val backgroundViewModel by viewModels<BackgroundViewModel>()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                backgroundViewModel.backgroundFlow.collectLatest { path ->
                    backgroundManager.drawable = path?.let {
                        loadDrawable(
                            this@MainActivity,
                            "https://image.tmdb.org/t/p/original${path}",
                        )
                    }
                }
            }
        }
    }
}