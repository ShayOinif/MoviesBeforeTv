package com.shayo.moviesbeforetv.tv

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Prompt.CONTINUE
import com.shayo.movies.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val LOGOUT = 1L
private const val CANCEL = 2L
private const val LOGIN = 3L

@AndroidEntryPoint
class LoginFragment : GuidedStepSupportFragment() {
    @Inject
    lateinit var userRepository: UserRepository

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return userRepository.getCurrentUser()?.let { user ->
            GuidanceStylist.Guidance(
                "Hello ${user.displayName}",
                "Email: ${user.email}",
                "You are logged in",
                activityViewModels<UserImageViewModel>().value.userImage
            )
        } ?: GuidanceStylist.Guidance(
            "Hello!",
            "Would you like to login/signup?",
            "You are not logged in",
            ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_account_circle_24)
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)

        userRepository.getCurrentUser()?.let {
            actions.add(
                GuidedAction.Builder(requireContext())
                    .id(LOGOUT)
                    .title("Logout")
                    .description("See you next time")
                    .build()
            )
        } ?: actions.add(
            GuidedAction.Builder(requireContext())
                .id(LOGIN)
                .title("Login/Signup")
                .description("Good to have you back")
                .build()
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(CANCEL)
                .title("Go Back")
                .description("Return to home")
                .build()
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction?) {
        super.onGuidedActionClicked(action)

        when (action?.id) {
            CANCEL -> {
                findNavController().popBackStack()
            }
            LOGOUT -> {
                AuthUI.getInstance()
                    .signOut(requireContext())

                findNavController().popBackStack()
            }
            LOGIN -> {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                )

                // Create and launch sign-in intent
                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build()

                signInLauncher.launch(signInIntent)
            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        findNavController().popBackStack()

        if (result.resultCode != RESULT_OK) {
            Log.d("User", "${result.idpResponse?.error?.localizedMessage}")
        }
    }
}