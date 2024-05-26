package com.randeep.moviedb.data.remote.movie.model

import com.squareup.moshi.Json

data class MovieRemote(

        @Json(name = "imdbID")
        val imdbId: String,

        @Json(name = "Title")
        val title: String,

        @Json(name = "Year")
        val year: String,

        @Json(name = "Type")
        val type: String,

        @Json(name = "Poster")
        val posterUrl: String
)