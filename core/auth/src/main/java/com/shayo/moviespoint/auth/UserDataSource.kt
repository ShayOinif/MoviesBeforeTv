package com.shayo.moviespoint.auth

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

// TODO: DI
class UserDataSource private constructor() {

    private val scope = CoroutineScope(SupervisorJob())

    val currentUserFlow = callbackFlow {
        val listener =
            AuthStateListener {
                // TODO: Handle the nullness of the email
                trySend(it.currentUser?.run { AuthUser(displayName!!, email!!, photoUrl) })
            }

        FirebaseAuth.getInstance().addAuthStateListener(listener)

        awaitClose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(1_500), 1)

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser?.run {
        AuthUser(displayName!!, email!!, photoUrl)
    }

    companion object {
        @Volatile
        private var INSTANCE: UserDataSource? = null

        fun getUserDataSource() =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserDataSource().also {
                    INSTANCE = it
                }
            }
    }
}

data class AuthUser(
    val displayName: String,
    val email: String,
    val photoUrl: Uri?
)