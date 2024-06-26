package com.randeep.moviedb.ui.movieList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randeep.moviedb.data.MovieRepository
import com.randeep.moviedb.data.model.Movie
import com.randeep.moviedb.data.remote.networkUtil.RemoteError
import com.randeep.moviedb.data.remote.networkUtil.RemoteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListSearchViewModel @Inject constructor(
        private val movieDbRepository: MovieRepository
) : ViewModel() {

        private val _movieList = MutableLiveData<ArrayList<Movie>>()
        val movieList: LiveData<ArrayList<Movie>> = _movieList

        private val _loading = MutableLiveData(false)
        val loading: LiveData<Boolean> = _loading

        private val _remoteError = MutableLiveData<RemoteError>()
        val remoteError: LiveData<RemoteError> = _remoteError

        private var pageNumber = 0
        private var lastSearchedText = ""
        private var totalNumberOfResults = 0


        fun searchMovieList(searchText: String) {

                // if search text is new, then clearing all the properties related to movie list
                if (pageNumber == 0 || lastSearchedText != searchText) {
                        _loading.value = true
                        lastSearchedText = searchText
                        pageNumber = 0
                        _movieList.value = arrayListOf()
                        getMovieList()
                }
        }

        fun getMovieList() {

                viewModelScope.launch {
                        when (val result =
                                movieDbRepository.getMovieListBySearch(
                                        lastSearchedText,
                                        ++pageNumber
                                )) {
                                is RemoteResult.Success -> {
                                        val movies = _movieList.value ?: arrayListOf()

                                        totalNumberOfResults = result.value.totalResults

                                        val moviesResult = result.value.movies as ArrayList<Movie>
                                        movies.addAll(moviesResult)
                                        _movieList.value = movies
                                        _loading.value = false
                                }

                                is RemoteResult.Error -> {
                                        _remoteError.value = result.remoteError
                                        _loading.value = false
                                }
                        }
                }
        }

        fun getPageNumber(): Int {
                return pageNumber
        }

        fun getTotalNumberOfSearchResults(): Int {
                return totalNumberOfResults
        }

        // trying to call api again if there is slow or no internet connection
        fun retryApiCall() {
                pageNumber--
                if (pageNumber == 0) _loading.value = true
                getMovieList()
        }
}