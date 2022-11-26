package com.shayo.moviesbeforetv.tv

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.accessibility.CaptioningManager
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val TAG = "MyTag"

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        (getSystemService(Context.CAPTIONING_SERVICE) as? CaptioningManager)?.addCaptioningChangeListener(
            object : CaptioningManager.CaptioningChangeListener() {
                override fun onEnabledChanged(enabled: Boolean) {
                    super.onEnabledChanged(enabled)
                    Log.d(TAG, "onEnabledChanged, enabled: $enabled")
                }

                override fun onLocaleChanged(locale: Locale?) {
                    super.onLocaleChanged(locale)
                    Log.d(TAG, "onLocaleChanged, Locale: $locale")
                }

                override fun onFontScaleChanged(fontScale: Float) {
                    super.onFontScaleChanged(fontScale)
                    Log.d(TAG, "onFontScaleChanged, scale: $fontScale")
                }

                override fun onUserStyleChanged(userStyle: CaptioningManager.CaptionStyle) {
                    super.onUserStyleChanged(userStyle)
                    Log.d(TAG, "onUserStyleChanged, style: $userStyle")
                    /*currentCaptionStyle.hasBackgroundColor = userStyle.hasBackgroundColor();
                            currentCaptionStyle.backgroundColor = userStyle.backgroundColor;
                            currentCaptionStyle.backgroundOpcity = userStyle.backgroundColor >>> 24;
                            currentCaptionStyle.hasForegroundColor = userStyle.hasForegroundColor();
                            currentCaptionStyle.foregroundColor = userStyle.foregroundColor;
                            currentCaptionStyle.foregroundOpacity = userStyle.foregroundColor >>> 24;
                            currentCaptionStyle.hasWindowColor = userStyle.hasWindowColor();
                            currentCaptionStyle.windowColor = userStyle.windowColor;
                            currentCaptionStyle.windowOpcity = userStyle.windowColor >>> 24;
                            currentCaptionStyle.hasEdgeColor = userStyle.hasEdgeColor();
                            currentCaptionStyle.edgeColor = userStyle.edgeColor;
                            currentCaptionStyle.hasEdgeType = userStyle.hasEdgeType();
                            currentCaptionStyle.edgeType = userStyle.edgeType;
                            currentCaptionStyle.typeFace = userStyle.getTypeface();*/
                }

            }
        )
    }
}