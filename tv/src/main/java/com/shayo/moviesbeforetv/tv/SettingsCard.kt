package com.shayo.moviesbeforetv.tv

import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide

internal sealed interface SettingsCardType {
    data class Account(
        val userImage: String?,
    ) : SettingsCardType
}

internal class SettingsCard(width: Int) : Presenter() {

    private val width: Int = width / 6

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {

        val cardView = ImageCardView(parent?.context)

        cardView.setMainImageDimensions(width, width)
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER)

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val cardView = viewHolder?.view as ImageCardView

        when (item) {
            is SettingsCardType.Account -> {
                cardView.titleText = "Account"

                item.userImage?.let {
                    Glide.with(viewHolder.view.context)
                        .load(it)
                        .centerCrop()
                        .into(cardView.mainImageView)
                } ?: cardView.mainImageView.setImageResource(R.drawable.ic_baseline_account_circle_24)
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        val cardView = viewHolder?.view as ImageCardView
        cardView.mainImage = null
    }
}