package com.randeep.moviedb.data.mapper

import com.randeep.moviedb.data.model.Movie
import com.randeep.moviedb.data.model.MovieList
import com.randeep.moviedb.data.remote.movie.model.MovieRemote
import com.randeep.moviedb.data.remote.movie.model.MovieSearchResponse

fun MovieRemote.asDomainModel() =  Movie(
        imdbId = imdbId,

        title = title,

        year = year,

        type = type,

        posterUrl = posterUrl
)


fun MovieSearchResponse.asDomainModel() = MovieList(

        movies = movies?.map { it.asDomainModel() } ?: emptyList(),

        totalResults = totalResults?.toInt() ?: 0,

        response = response.toBoolean(),

        errorMessage = errorMessage
)