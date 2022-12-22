package com.shayo.moviespoint.person

import com.shayo.movies.mapToMovieWithoutGenres
import com.shayo.network.NetworkMovie
import com.shayo.network.PersonNetworkService
import com.shayo.network.getBio
import javax.inject.Inject

internal interface NetworkPersonDataSource {
    suspend fun getBio(id: Int): Result<Person>
}

internal class NetworkPersonDataSourceImpl @Inject constructor(
    private val personNetworkService: PersonNetworkService,
) : NetworkPersonDataSource {

    override suspend fun getBio(id: Int): Result<Person> {
        return personNetworkService.getBio(id = id).map { personNetworkServiceResponse ->
            with(personNetworkServiceResponse) {
                Person(
                    id = id,
                    biography = biography,
                    name = name,
                    profilePath = profilePath,
                    combinedCredits = CombinedCredits(
                        cast = combinedCredits.cast.map(NetworkMovie<Int>::mapToMovieWithoutGenres),
                        crew = combinedCredits.crew.map(NetworkMovie<Int>::mapToMovieWithoutGenres),
                    )
                )
            }
        }
    }
}