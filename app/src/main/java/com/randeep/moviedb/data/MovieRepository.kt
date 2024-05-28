package com.randeep.moviedb.data

import com.randeep.moviedb.data.mapper.asDomainModel
import com.randeep.moviedb.data.model.MovieList
import com.randeep.moviedb.data.remote.movie.MovieRemoteDataSource
import com.randeep.moviedb.data.remote.networkUtil.HTTPError
import com.randeep.moviedb.data.remote.networkUtil.Other
import com.randeep.moviedb.data.remote.networkUtil.RemoteResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface MovieRepository {

        suspend fun getMovieListBySearch(searchText: String, pageNumber: Int): RemoteResult<MovieList>
}


class MovieRepositoryImpl @Inject constructor(
        private val remoteDataSource: MovieRemoteDataSource,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): MovieRepository {
        override suspend fun getMovieListBySearch(
                searchText: String,
                pageNumber: Int
        ): RemoteResult<MovieList> = withContext(ioDispatcher) {

                when(val result = remoteDataSource.getMovieListBySearch(searchText, pageNumber)) {
                        is RemoteResult.Success -> {
                                val movieList = result.value.asDomainModel()
                                if(movieList.response) {
                                        return@withContext RemoteResult.Success(movieList)
                                } else {
                                        // if movie list is empty, send remote error with error message
                                        return@withContext RemoteResult.Error(Other(Throwable(), message = movieList.errorMessage))
                                }

                        }
                        is RemoteResult.Error -> {
                                return@withContext RemoteResult.Error(result.remoteError)
                        }
                }
        }

}