package com.randeep.moviedb.data.remote.movie

import com.randeep.moviedb.data.remote.movie.model.MovieSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {

        @GET("/")
        suspend fun getSearchMovie(
                @Query("s") searchText: String,
                @Query("page") page: Int,
                @Query("apikey") apiKey: String
        ): MovieSearchResponse
}