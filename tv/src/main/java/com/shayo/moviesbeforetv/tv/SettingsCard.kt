package com.shayo.moviesbeforetv.tv

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.allViews
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide

internal sealed interface SettingsCardType {
    data class Account(
        val userImage: String?,
    ) : SettingsCardType

    data class Usage(
        val enabled: Boolean
    ) : SettingsCardType
}

internal class SettingsCard(width: Int) : Presenter() {

    private val width: Int = width / 6

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {

        val cardView = ImageCardView(parent?.context)

        cardView.setMainImageDimensions(width, width)
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER)

        cardView.allViews.filter { it is TextView }.forEach { (it as TextView).maxLines = 2 }

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
                }
                    ?: cardView.mainImageView.setImageResource(R.drawable.ic_baseline_account_circle_24)
            }
            is SettingsCardType.Usage -> {
                cardView.titleText = cardView.context.getString(
                    R.string.usage,
                    cardView.context.getString(if (item.enabled) R.string.enabled else R.string.disabled)
                )

                cardView.mainImageView.setImageResource(
                    if (item.enabled) R.drawable.ic_baseline_bar_chart_24 else R.drawable.ic_baseline_bar_chart_disabled_24
                )
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        val cardView = viewHolder?.view as ImageCardView
        cardView.mainImage = null
    }
}