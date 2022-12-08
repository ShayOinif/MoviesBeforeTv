package com.shayo.moviespoint.auth

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserDataSource private constructor(){

    private val _currentUserFlow = MutableStateFlow<User?>(null)

    val currentUserFlow: StateFlow<User?> = _currentUserFlow

    internal fun updateUser(firebaseUser: FirebaseUser?) {

        Log.d("User", "${firebaseUser?.email}")

        _currentUserFlow.value = firebaseUser?.email?.let { User(it) }
    }

    fun logout() {
        updateUser(null)
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

data class User(
    val email: String,
)