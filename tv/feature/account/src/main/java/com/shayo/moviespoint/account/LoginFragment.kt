package com.shayo.moviespoint.account

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import com.shayo.movies.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : GuidedStepSupportFragment() {
    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return userRepository.getCurrentUser()?.let { user ->
            GuidanceStylist.Guidance(
                "Hello ${user.displayName}",
                "Email: ${user.email}",
                "You are logged in",
                null
            )
        } ?: throw Exception()
    }
}