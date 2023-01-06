package com.shayo.moviespoint.data.usage

import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    val usageEnabledFlow: Flow<Boolean>

    fun changeUsage(enabled: Boolean)

    val shouldAskFlow: Flow<Boolean>

    fun markAsked()
}