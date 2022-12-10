package com.shayo.moviesbeforetv.tv.utils

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

fun loadDrawable(
    fragment: Fragment,
    url: String?,
    drawableCallback: (drawable: Drawable?) -> Unit,
) {
    Glide.with(fragment)
        .load(url)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                drawableCallback(resource)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                drawableCallback(null)
            }

            // TODO:
            override fun onLoadCleared(placeholder: Drawable?) {}
        })
}