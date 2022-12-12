package com.shayo.moviesbeforetv.tv

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.leanback.app.BackgroundManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shayo.moviesbeforetv.tv.utils.loadDrawable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

private const val DEFAULT_UPDATE_TIME = 1_000L

interface FragmentWithBackground {
    var backgroundManager: BackgroundManager
    var backgroundFlow: MutableStateFlow<Background?>

    @OptIn(FlowPreview::class)
    fun setupBackgroundUpdate(
        lifecycleOwner: LifecycleOwner,
        fragment: Fragment,
        activity: Activity,
        timeout: Long = DEFAULT_UPDATE_TIME,
    ) {
        backgroundManager = BackgroundManager.getInstance(activity)

        if (!backgroundManager.isAttached) {
            backgroundManager.attach(activity.window)
        }

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                backgroundFlow.debounce(timeout).collectLatest { background ->

                    background?.let {
                        when (background) {
                            is Background.HasBackground -> {
                                loadDrawable(
                                    fragment,
                                    "https://image.tmdb.org/t/p/original${background.path}",
                                ) { drawable ->
                                    if (backgroundManager.drawable != drawable)
                                        backgroundManager.drawable = drawable
                                }
                            }
                            is Background.NoBackground -> {
                                backgroundManager.drawable = null
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateNow(fragment: Fragment) {
        if (backgroundManager.drawable == null) {
            loadDrawable(
                fragment,
                "https://image.tmdb.org/t/p/original${backgroundFlow.value}",
            ) { drawable ->
                if (backgroundManager.drawable != drawable) {
                    backgroundManager.drawable = drawable
                }
            }
        }
    }
}

sealed interface Background {
    data class HasBackground(
        val path: String
    ) : Background
    object NoBackground : Background
}