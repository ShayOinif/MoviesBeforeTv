package com.shayo.movies

import com.shayo.moviespoint.auth.UserDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface UserRepository {
    val currentAuthUserFlow: Flow<User?>

    fun signOut()
}

internal class UserRepositoryImpl(private val userDataSource: UserDataSource = UserDataSource.getUserDataSource()) : UserRepository {
    override val currentAuthUserFlow = userDataSource.currentUserFlow
        .map { currentUser ->
            currentUser?.run { User(displayName, email, photoUrl) }
        }

    override fun signOut() {
        userDataSource.signOut()
    }
}