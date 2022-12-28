package com.shayo.moviespoint.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shayo.movies.MoviesRepository
import com.shayo.moviespoint.getcategoriesflows.categories
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UpdateCategoriesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val moviesRepository: MoviesRepository,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("MyTAg", "doWork")

        categories.forEach { category ->
            if (!moviesRepository.updateCategory(
                    type = category.type,
                    category = category.category,
                )
            )
                return Result.retry()
        }

        Log.d("MyTAg", "done")
        return Result.success()
    }
}