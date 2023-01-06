package com.shayo.moviespoint.data.usage

import com.shayo.moviespoint.core.usage.UsageService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object UsageModule {

    @Provides
    fun provideUsageRepository(usageService: UsageService): UsageRepository =
        UsageRepositoryImpl(usageService)
}