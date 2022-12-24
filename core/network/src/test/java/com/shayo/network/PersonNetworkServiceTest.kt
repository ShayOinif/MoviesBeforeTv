package com.shayo.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class PersonNetworkServiceTest {

    @Inject
    private val personNetworkService: PersonNetworkService = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(PersonNetworkService::class.java)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun networkPersonDataSource_correctPersonId_okResponse() = runTest {
        val sam = personNetworkService.getBio(id = 65731).fold(
            onSuccess = { it },
            onFailure = {
                null
            }
        )

        assert(sam?.name?.contains("Sam") == true)
    }
}