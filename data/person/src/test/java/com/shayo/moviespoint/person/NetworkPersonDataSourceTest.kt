package com.shayo.moviespoint.person

import com.shayo.network.NetworkCombinedCredits
import com.shayo.network.PersonNetworkService
import com.shayo.network.PersonNetworkServiceResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NetworkPersonDataSourceTest {

    private val networkPersonDataSource = NetworkPersonDataSourceImpl(
        object : PersonNetworkService {
            override suspend fun getBioRet(
                id: Int,
                apiKey: String,
                append: String
            ): PersonNetworkServiceResponse {
                return PersonNetworkServiceResponse(
                    id = 65731,
                    biography = "Some bio",
                    name = "Sam",
                    profilePath = null,
                    combinedCredits = NetworkCombinedCredits(
                        cast = emptyList(),
                        crew = emptyList()
                    )
                )
            }
        }
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun networkPersonDataSource_correctPersonId_okResponse() = runTest {
        val sam = networkPersonDataSource.getBio(id = 65731).fold(
            onSuccess = {
                it
            },
            onFailure = {
                null
            }
        )

        assert(sam?.name?.contains("Sam") == true)
    }
}