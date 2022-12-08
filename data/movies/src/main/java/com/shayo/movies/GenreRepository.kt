package com.shayo.movies

import com.shayo.moviepoint.db.DbGenre
import com.shayo.moviepoint.db.LocalGenresDataSource
import com.shayo.network.NetworkGenreDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

// TODO: Move to another module
// TODO: Handle tv shows genres
interface GenreRepository {
    val movieGenresFlow: Flow<Map<Int, Genre>>
}

internal class GenreRepositoryImpl constructor(
    private val networkGenreDataSource: NetworkGenreDataSource,
    private val localGenresDataSource: LocalGenresDataSource,
) : GenreRepository {

    private var didTry = false

    override val movieGenresFlow = localGenresDataSource.genresFlow
        .onEach {
            if (it.isEmpty() && !didTry) {
                didTry = true

                networkGenreDataSource.getMoviesGenres()
                    .map { networkGenres ->
                        localGenresDataSource.addGenres(
                            networkGenres.map { networkGenre ->
                                DbGenre(
                                    networkGenre.id,
                                    networkGenre.name,
                                )
                            }
                        )
                    }
            }
        }
        .map { dbGenres ->

            dbGenres.map { dbGenre ->
                Genre(
                    dbGenre.id,
                    dbGenre.name
                )
            }.associateBy {
                it.id
            }
        }
}