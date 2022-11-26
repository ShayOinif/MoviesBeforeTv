package com.shayo.moviesbeforetv.tv

import android.os.Bundle
import androidx.leanback.app.ErrorSupportFragment

class ErrorFragment : ErrorSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = resources.getString(R.string.app_name)
    }

    internal fun setErrorContent() {
       /* imageDrawable =
            ContextCompat.getDrawable(activity!!, androidx.leanback.R.drawable.lb_ic_sad_cloud)
        message = "E"
        setDefaultBackground(TRANSLUCENT)

        buttonText = resources.getString(R.string.dismiss_error)
        buttonClickListener = View.OnClickListener {
            fragmentManager!!.beginTransaction().remove(this@ErrorFragment).commit()
        }*/
    }

    companion object {
        private val TRANSLUCENT = true
    }
}