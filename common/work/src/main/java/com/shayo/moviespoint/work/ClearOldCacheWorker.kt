package com.shayo.moviespoint.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shayo.movies.FavoritesRepository
import com.shayo.movies.MoviesRepository
import com.shayo.movies.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ClearOldCacheWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val moviesRepository: MoviesRepository,
    private val favoritesRepository: FavoritesRepository,
    private val userRepository: UserRepository,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {

        moviesRepository.deleteOldMovies(
            moviesRepository.getCategorizedMoviesIds()
                .plus(favoritesRepository.getAllFavoritesIds(userRepository.getCurrentUser()?.email))
                .associateBy {
                    it
                }.mapValues<Int, Int, Void?> { null }
        )

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}

