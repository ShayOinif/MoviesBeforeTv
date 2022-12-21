package com.shayo.movies

import com.shayo.network.NetworkCreditsDataSource

interface CreditsRepository {
    suspend fun getCredits(type: String, id: Int): Result<TopCastAndDirector>
}

data class TopCastAndDirector(
    val cast: List<Credit>,
    val director: Credit?,
)

class CreditsRepositoryImpl(
    private val networkCreditsDataSource: NetworkCreditsDataSource,
) : CreditsRepository {
    override suspend fun getCredits(type: String, id: Int): Result<TopCastAndDirector> {
        return networkCreditsDataSource.getCredits(type, id)
            .map { response ->
                TopCastAndDirector(
                    cast = response.cast.map { networkCredit ->
                        with(networkCredit) {
                            Credit(
                                id,
                                name,
                                profilePath,
                                description
                            )
                        }
                    },
                    director = response.crew.firstOrNull { networkCredit ->
                        networkCredit.description == "Director"
                    }?.let { networkCredit ->
                        with(networkCredit) {
                            Credit(
                                id,
                                name,
                                profilePath,
                                description
                            )
                        }
                    }
                )
            }
    }
}