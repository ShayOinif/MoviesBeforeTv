package com.shayo.moviesbeforetv.tv.utils

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

suspend fun loadDrawable(
    fragment: Fragment,
    url: String?,
    crop: Boolean = false
): Drawable? {
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            if (crop) {
                Glide.with(fragment)
                    .load(url)
                    .circleCrop()
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            cont.resume(resource)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            cont.resume(null)
                        }

                        // TODO:
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                Glide.with(fragment)
                    .load(url)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            cont.resume(resource)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            cont.resume(null)
                        }

                        // TODO:
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
    }
}

suspend fun loadDrawable(
    activity: FragmentActivity,
    url: String?,
): Drawable? {
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            Glide.with(activity)
                .load(url)
                .into(
                    object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            cont.resume(resource)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            cont.resume(null)
                        }

                        // TODO:
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    }
                )
        }
    }
}