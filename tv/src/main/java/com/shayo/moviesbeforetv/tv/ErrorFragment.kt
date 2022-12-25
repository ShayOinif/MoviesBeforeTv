package com.shayo.moviesbeforetv.tv

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.ErrorSupportFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

class ErrorFragment : ErrorSupportFragment() {

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = resources.getString(R.string.app_name)

        imageDrawable =
            ContextCompat.getDrawable(
                requireActivity(),
                androidx.leanback.R.drawable.lb_ic_sad_cloud
            )
        message = "Error: ${navArgs<ErrorFragmentArgs>().value.message}"

        setDefaultBackground(true)

        buttonText = resources.getString(R.string.dismiss_error)
        buttonClickListener = View.OnClickListener {
            findNavController().popBackStack()
        }
    }
}