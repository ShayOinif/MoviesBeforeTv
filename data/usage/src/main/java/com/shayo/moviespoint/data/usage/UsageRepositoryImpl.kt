package com.shayo.moviespoint.data.usage

import com.shayo.moviespoint.core.usage.UsageService

internal class UsageRepositoryImpl(
    private val usageService: UsageService,
) : UsageRepository {
    override val usageEnabledFlow = usageService.crashEnabledFlow

    override fun changeUsage(enabled: Boolean) = usageService.changeUsage(enabled)

    override val shouldAskFlow = usageService.shouldAskFlow

    override fun markAsked() = usageService.markAsked()
}