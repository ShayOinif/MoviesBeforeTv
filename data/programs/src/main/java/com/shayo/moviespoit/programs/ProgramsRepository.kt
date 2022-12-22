package com.shayo.moviespoit.programs

interface ProgramsRepository {
    suspend fun getAll(): List<Program>

    suspend fun add(program: Program)

    suspend fun deleteAll()
}

internal class ProgramsRepositoryImpl(
    private val localProgramsDataSource: LocalProgramsDataSource,
) : ProgramsRepository {
    override suspend fun getAll(): List<Program> {
        return localProgramsDataSource.getAll()
    }

    override suspend fun add(program: Program) {
        localProgramsDataSource.add(program)
    }

    override suspend fun deleteAll() {
        localProgramsDataSource.deleteAll()
    }
}