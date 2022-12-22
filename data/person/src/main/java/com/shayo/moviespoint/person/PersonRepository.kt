package com.shayo.moviespoint.person

interface PersonRepository {
    suspend fun getBio(id: Int): Result<Person>
}

internal class PersonRepositoryImpl(
    private val networkPersonDataSource: NetworkPersonDataSource,
) : PersonRepository {
    override suspend fun getBio(id: Int) =
        networkPersonDataSource.getBio(id = id)
}