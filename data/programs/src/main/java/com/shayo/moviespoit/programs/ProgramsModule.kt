package com.shayo.moviespoit.programs

import com.shayo.moviepoint.db.ProgramsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object ProgramsModule {
    @Provides
    fun provideProgramsRepository(programsDao: ProgramsDao): ProgramsRepository =
        ProgramsRepositoryImpl(
            LocalProgramsDataSourceImpl(programsDao)
        )
}