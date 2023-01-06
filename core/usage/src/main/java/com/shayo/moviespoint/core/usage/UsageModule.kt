package com.shayo.moviespoint.core.usage

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object UsageModule {

    @Provides
    fun provideUsageService(
        @ApplicationContext
        context: Context,
    ): UsageService =
        UsageServiceImpl(
            dataStore = context.dataStore
        )
}