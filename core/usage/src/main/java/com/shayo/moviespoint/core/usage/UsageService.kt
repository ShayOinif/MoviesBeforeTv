package com.shayo.moviespoint.core.usage

import kotlinx.coroutines.flow.Flow

interface UsageService {
    val crashEnabledFlow: Flow<Boolean>

    val shouldAskFlow: Flow<Boolean>

    fun markAsked()

    fun changeUsage(enabled: Boolean)
}

