package com.shayo.moviespoint.person

import com.shayo.network.PersonNetworkService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object PersonModule {
    @Provides
    fun providePersonRepository(personNetworkService: PersonNetworkService): PersonRepository =
        PersonRepositoryImpl(
            NetworkPersonDataSourceImpl(personNetworkService)
        )
}