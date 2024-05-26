package com.randeep.moviedb.data.remote.movie.model

import com.squareup.moshi.Json

data class MovieSearchResponse(

        @Json(name = "Search")
        val movies: List<MovieRemote>?,

        val totalResults: String?,

        @Json(name = "Response")
        val response: String,

        @Json(name = "Error")
        val errorMessage: String?
)
