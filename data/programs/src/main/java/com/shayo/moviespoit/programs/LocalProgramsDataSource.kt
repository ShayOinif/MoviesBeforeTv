package com.shayo.moviespoit.programs

import com.shayo.moviepoint.db.DbProgram
import com.shayo.moviepoint.db.ProgramsDao

internal interface LocalProgramsDataSource {
    suspend fun getAll(): List<Program>

    suspend fun add(program: Program)

    suspend fun deleteAll()
}

internal class LocalProgramsDataSourceImpl(
    private val programsDao: ProgramsDao,
) : LocalProgramsDataSource {
    override suspend fun getAll(): List<Program> {
        return programsDao.getAll().map {
            Program(it.id)
        }
    }

    override suspend fun add(program: Program) {
        programsDao.add(
            DbProgram(
                program.id
            )
        )
    }

    override suspend fun deleteAll() {
        programsDao.deleteAll()
    }

}