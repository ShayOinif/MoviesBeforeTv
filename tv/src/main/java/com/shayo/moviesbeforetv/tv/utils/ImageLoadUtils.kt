package com.shayo.moviesbeforetv.tv.utils

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun loadDrawable(
    fragment: Fragment,
    url: String?,
) : Drawable? {
    return suspendCoroutine { cont ->
        Glide.with(fragment)
            .load(url)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
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

suspend fun loadDrawable(
    activity: FragmentActivity,
    url: String?,
) : Drawable? {
    return suspendCoroutine { cont ->
        Glide.with(activity)
            .load(url)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
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