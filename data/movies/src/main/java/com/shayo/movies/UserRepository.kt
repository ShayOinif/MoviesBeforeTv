package com.shayo.movies

import com.shayo.moviespoint.auth.UserDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface UserRepository {
    val currentAuthUserFlow: Flow<User?>

    fun signOut()

    fun getCurrentUser(): User?
}

internal class UserRepositoryImpl(private val userDataSource: UserDataSource = UserDataSource.getUserDataSource()) :
    UserRepository {
    override val currentAuthUserFlow = userDataSource.currentUserFlow
        .map { currentUser ->
            currentUser?.run { User(displayName, email, photoUrl) }
        }.flowOn(Dispatchers.IO)

    override fun signOut() {
        userDataSource.signOut()
    }

    override fun getCurrentUser(): User? {
        return userDataSource.getCurrentUser()?.run { User(displayName, email, photoUrl) }
    }
}