package com.randeep.moviedb.data.remote.movie

import com.randeep.moviedb.data.remote.movie.model.MovieSearchResponse
import com.randeep.moviedb.data.remote.networkUtil.Constants
import com.randeep.moviedb.data.remote.networkUtil.RemoteResult
import com.randeep.moviedb.data.remote.networkUtil.safeApiCall
import javax.inject.Inject

interface MovieRemoteDataSource {

        suspend fun getMovieListBySearch(
                searchText: String,
                pageNumber: Int
        ): RemoteResult<MovieSearchResponse>
}

class MovieRemoteDataSourceImpl @Inject constructor(
        private val movieApi: MovieApi
) : MovieRemoteDataSource {
        override suspend fun getMovieListBySearch(
                searchText: String,
                pageNumber: Int
        ): RemoteResult<MovieSearchResponse> {
                return safeApiCall { movieApi.getSearchMovie(searchText, pageNumber, Constants.API_KEY) }
        }

}