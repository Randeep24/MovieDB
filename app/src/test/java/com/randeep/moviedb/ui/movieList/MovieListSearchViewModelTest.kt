package com.randeep.moviedb.ui.movieList

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.randeep.moviedb.R
import com.randeep.moviedb.data.MovieRepository
import com.randeep.moviedb.data.mapper.asDomainModel
import com.randeep.moviedb.data.remote.movie.model.MovieSearchResponse
import com.randeep.moviedb.data.remote.networkUtil.Other
import com.randeep.moviedb.data.remote.networkUtil.RemoteError
import com.randeep.moviedb.data.remote.networkUtil.RemoteResult
import com.randeep.moviedb.data.remote.networkUtil.TimeOut
import com.randeep.moviedb.utils.FileReaderUtils
import com.randeep.moviedb.utils.getOrAwaitValue
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListSearchViewModelTest {

        // for executing each task synchronously
        @get:Rule
        val instantExecutorRule = InstantTaskExecutorRule()

        // mocking MovieRepository for sending it in viewModel constructor
        private val mockMovieRepository = mockk<MovieRepository>()
        private lateinit var viewModel: MovieListSearchViewModel
        private lateinit var moshiAdapter: JsonAdapter<MovieSearchResponse>

        // CoroutineDispatcher to run view model functions
        private val mainThreadSurrogate = newSingleThreadContext("UI thread")

        @Before
        fun setUp() {

                // setting dispatcher as Main thread
                Dispatchers.setMain(mainThreadSurrogate)

                viewModel = MovieListSearchViewModel(mockMovieRepository)

                /**
                 * setting up moshi adapter for converting string into json
                 */
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                moshiAdapter =
                        moshi.adapter(MovieSearchResponse::class.java)
        }

        @After
        fun afterTests() {
                Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
                mainThreadSurrogate.close()
                unmockkAll()
        }

        /**
         * Test a successful response for fetching movie list and observing search data LiveData
         */
        @Test
        fun `get searched movie list successfully`() = runBlocking {
                val fileData = FileReaderUtils.readTestResourceFile("search_result.json")
                val searchListResponse = moshiAdapter.fromJson(fileData)!!
                val movieList = searchListResponse.asDomainModel()

                coEvery {
                        mockMovieRepository.getMovieListBySearch(
                                any(),
                                any()
                        )
                } returns RemoteResult.Success(
                        movieList
                )

                viewModel.searchMovieList("game")

                val movieListLiveData = viewModel.movieList.getOrAwaitValue()
                assertTrue(movieListLiveData?.isNotEmpty()!!)

                assertEquals(
                        "Game of Thrones",
                        movieListLiveData[0].title
                )
                assertEquals(
                        "2011–2019",
                        movieListLiveData[0].year
                )
                assertEquals("series", movieListLiveData[0].type)

                assertEquals(
                        "Sherlock Holmes: A Game of Shadows",
                        movieListLiveData[3].title
                )

        }

        /**
         * Test a successful response for fetching search result with no movie
         */
        @Test
        fun `get searched data with empty movie list`() = runBlocking {
                val fileData = FileReaderUtils.readTestResourceFile("empty_search_result.json")
                val searchListResponse = moshiAdapter.fromJson(fileData)!!
                val movieList = searchListResponse.asDomainModel()

                coEvery {
                        mockMovieRepository.getMovieListBySearch(
                                any(),
                                any()
                        )
                } returns RemoteResult.Error(Other(Throwable(), message = movieList.errorMessage))

                viewModel.searchMovieList("abc")

                val remoteError = viewModel.remoteError.getOrAwaitValue()
                assertTrue(remoteError is Other)
                remoteError as Other

                assertEquals("Movie not found!", remoteError.message)
        }


        /**
         * Test a fail response for search movie list with [SocketTimeoutException] and observing remoteError variable
         */
        @Test
        fun `get searched movie list response as SocketTimeOutException`() = runBlocking {

                val remoteError = TimeOut(SocketTimeoutException())
                coEvery {
                        mockMovieRepository.getMovieListBySearch(
                                any(),
                                any()
                        )
                } returns RemoteResult.Error(remoteError)

                viewModel.searchMovieList("game")

                val remoteErrorLiveData = viewModel.remoteError.getOrAwaitValue()
                assertTrue(remoteErrorLiveData is RemoteError)
                val errorResult = remoteErrorLiveData as RemoteError

                assertTrue(errorResult.throwable is SocketTimeoutException)
                assertTrue(errorResult is TimeOut)
                assertEquals(
                        R.string.network_timed_out,
                        errorResult.messageResId
                )
        }
}