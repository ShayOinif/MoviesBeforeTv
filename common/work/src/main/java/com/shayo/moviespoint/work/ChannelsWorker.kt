package com.shayo.moviespoint.work

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.hilt.work.HiltWorker
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.ChannelLogoUtils
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shayo.movies.MoviesRepository
import com.shayo.moviespoit.programs.Program
import com.shayo.moviespoit.programs.ProgramsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// TODO: Extract duplicate code for movie and tv to functions or something

@HiltWorker
class ChannelsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val moviesRepository: MoviesRepository,
    private val programsRepository: ProgramsRepository,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {

        moviesRepository.discover("movie").fold(
            onSuccess = { movies ->
                moviesRepository.discover("tv").fold(
                    onSuccess = { shows ->
                        var movieId = applicationContext.getSharedPreferences(
                            "Default",
                            FragmentActivity.MODE_PRIVATE
                        ).getLong("movieId", -1L)

                        var tvId = applicationContext.getSharedPreferences(
                            "Default",
                            FragmentActivity.MODE_PRIVATE
                        ).getLong("movieId", -1L)


                        if (movieId == -1L) {
                            val builder = Channel.Builder()

                            builder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                                .setDisplayName("Discover Movies")
                                .setAppLinkIntentUri(Uri.parse("mopoint://point"))

                            val channelUri = applicationContext.contentResolver.insert(
                                TvContractCompat.Channels.CONTENT_URI,
                                builder.build().toContentValues()
                            )

                            channelUri?.let {
                                movieId = ContentUris.parseId(channelUri)

                                applicationContext.getSharedPreferences(
                                    "Default",
                                    FragmentActivity.MODE_PRIVATE
                                ).edit()
                                    .putLong("movieId", movieId)
                                    .apply()

                                ChannelLogoUtils.storeChannelLogo(
                                    applicationContext,
                                    movieId,
                                    ContextCompat.getDrawable(
                                        applicationContext,
                                        R.mipmap.ic_channel
                                    )!!.toBitmap()
                                )
                            } ?: return Result.failure()
                        }

                        if (tvId == -1L) {
                            val builder = Channel.Builder()

                            builder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                                .setDisplayName("Discover Tv Shows")
                                .setAppLinkIntentUri(Uri.parse("mopoint://point"))

                            val channelUri = applicationContext.contentResolver.insert(
                                TvContractCompat.Channels.CONTENT_URI,
                                builder.build().toContentValues()
                            )

                            channelUri?.let {
                                tvId = ContentUris.parseId(channelUri)

                                applicationContext.getSharedPreferences(
                                    "Default",
                                    FragmentActivity.MODE_PRIVATE
                                ).edit()
                                    .putLong("movieId", tvId)
                                    .apply()

                                ChannelLogoUtils.storeChannelLogo(
                                    applicationContext,
                                    tvId,
                                    ContextCompat.getDrawable(
                                        applicationContext,
                                        R.mipmap.ic_channel
                                    )!!.toBitmap()
                                )
                            } ?: return Result.failure()
                        }


                        programsRepository.getAll().forEach {
                            applicationContext.contentResolver
                                .delete(TvContractCompat.buildPreviewProgramUri(it.id), null, null)
                        }

                        programsRepository.deleteAll()

                        movies.forEach {
                            val builder = PreviewProgram.Builder()
                            builder.setChannelId(movieId)
                                .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
                                .setTitle(it.title)
                                .setDescription(it.overview)
                                .setPosterArtUri(Uri.parse("https://image.tmdb.org/t/p/w780${it.backdropPath}"))
                                .setIntentUri(Uri.parse("mopoint://point?movieId=${it.id}&movieType=movie"))

                            val programUri = applicationContext.contentResolver.insert(TvContractCompat.PreviewPrograms.CONTENT_URI,
                                builder.build().toContentValues())

                            programUri?.let {
                                val programId = ContentUris.parseId(programUri)

                                programsRepository.add(Program(programId))
                            }
                        }

                        shows.forEach {
                            val builder = PreviewProgram.Builder()
                            builder.setChannelId(tvId)
                                .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
                                .setTitle(it.title)
                                .setDescription(it.overview)
                                .setPosterArtUri(Uri.parse("https://image.tmdb.org/t/p/w780${it.backdropPath}"))
                                .setIntentUri(Uri.parse("mopoint://point?movieId=${it.id}&movieType=tv"))

                            val programUri = applicationContext.contentResolver.insert(TvContractCompat.PreviewPrograms.CONTENT_URI,
                                builder.build().toContentValues())

                            programUri?.let {
                                val programId = ContentUris.parseId(programUri)

                                programsRepository.add(Program(programId))
                            }
                        }

                        return Result.success()
                    },
                    onFailure = {
                        return Result.retry()
                    }
                )
            },
            onFailure = {
                return Result.retry()
            }
        )
    }
}