package com.shayo.moviespoint

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.shayo.moviespoint.work.ClearOldCacheWorker
import com.shayo.moviespoint.work.UpdateCacheWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MoviesPointHiltApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    override fun onCreate() {
        super.onCreate()

        val deleteRequest =
            PeriodicWorkRequestBuilder<ClearOldCacheWorker>(1, TimeUnit.DAYS)
                .build()

        val manager = WorkManager.getInstance(this)

        manager.enqueueUniquePeriodicWork(
            "clear_cache",
            ExistingPeriodicWorkPolicy.KEEP,
            deleteRequest
        )

        val updateRequest = PeriodicWorkRequestBuilder<UpdateCacheWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        manager.enqueueUniquePeriodicWork(
            "update_cache",
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )
    }
}