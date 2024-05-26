package com.randeep.moviedb.data.model

data class MovieList(

        val movies: List<Movie>,

        val totalResults: Int,

        val response: Boolean,

        val errorMessage: String?
)

