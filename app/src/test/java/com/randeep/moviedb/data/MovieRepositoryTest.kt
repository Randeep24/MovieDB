package com.randeep.moviedb.data

import com.randeep.moviedb.R
import com.randeep.moviedb.data.mapper.asDomainModel
import com.randeep.moviedb.data.remote.movie.MovieRemoteDataSource
import com.randeep.moviedb.data.remote.movie.model.MovieSearchResponse
import com.randeep.moviedb.data.remote.networkUtil.HTTPError
import com.randeep.moviedb.data.remote.networkUtil.IOError
import com.randeep.moviedb.data.remote.networkUtil.NoInternet
import com.randeep.moviedb.data.remote.networkUtil.Other
import com.randeep.moviedb.data.remote.networkUtil.RemoteResult
import com.randeep.moviedb.data.remote.networkUtil.TimeOut
import com.randeep.moviedb.utils.FileReaderUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MovieRepositoryTest {

        private val mockMovieRemoteDataSource = mockk<MovieRemoteDataSource>()
        private val movieRepository = MovieRepositoryImpl(mockMovieRemoteDataSource)
        private lateinit var moshiAdapter: JsonAdapter<MovieSearchResponse>

        @Before
        fun setup() {
                /**
                 * setting up moshi adapter for converting string into json
                 */
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                moshiAdapter =
                        moshi.adapter(MovieSearchResponse::class.java)
        }

        @After
        fun afterTests() {
                unmockkAll()
        }

        /**
         * Test a successful movie list response for searched text
         */
        @Test
        fun `get movie list success response for searched text`() = runBlocking {
                val fileData = FileReaderUtils.readTestResourceFile("search_result.json")
                val searchResponse = moshiAdapter.fromJson(fileData)!!

                val searchData = searchResponse.asDomainModel()
                coEvery { mockMovieRemoteDataSource.getMovieListBySearch(any(), any()) } returns RemoteResult.Success(
                        searchResponse
                )

                val result = movieRepository.getMovieListBySearch("game", 1)

                assertTrue(result is RemoteResult.Success)
                assertEquals(searchData, (result as RemoteResult.Success).value)

                val searchedMovieList = result.value

                assertTrue(searchedMovieList.response)
                assertEquals(
                        "Game of Thrones",
                        searchedMovieList.movies[0].title
                )
                assertEquals(
                        "2011–2019",
                        searchedMovieList.movies[0].year
                )
                assertEquals("series", searchedMovieList.movies[0].type)

                assertEquals(
                        "Sherlock Holmes: A Game of Shadows",
                        searchedMovieList.movies[3].title
                )
        }


        /**
         * Test a successful response for no movie found result
         */
        @Test
        fun `get a response for no movie found result`() = runBlocking {
                val fileData = FileReaderUtils.readTestResourceFile("empty_search_result.json")
                val searchResponse = moshiAdapter.fromJson(fileData)!!

                val searchData = searchResponse.asDomainModel()
                coEvery { mockMovieRemoteDataSource.getMovieListBySearch(any(), any()) } returns RemoteResult.Success(
                        searchResponse
                )

                val result = movieRepository.getMovieListBySearch("abc", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError is Other)
                val remoteError = errorResult.remoteError as Other
                assertEquals(
                        "Movie not found!",
                        remoteError.message
                )
        }


        /**
         * Test a fail response for search with [SocketTimeoutException]
         */
        @Test
        fun `get search response as SocketTimeOutException`() = runBlocking {

                val remoteError = TimeOut(SocketTimeoutException())
                coEvery { mockMovieRemoteDataSource.getMovieListBySearch(any(), any()) } returns  RemoteResult.Error(remoteError)

                val result = movieRepository.getMovieListBySearch("game", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is SocketTimeoutException)
                assertTrue(errorResult.remoteError is TimeOut)
                assertEquals(
                        R.string.network_timed_out,
                        errorResult.remoteError.messageResId
                )
        }

        /**
         * Test a fail response for search with [UnknownHostException]
         */
        @Test
        fun `get search response as UnknownHostException`() = runBlocking {

                val remoteError = NoInternet(UnknownHostException())
                coEvery { mockMovieRemoteDataSource.getMovieListBySearch(any(), any()) } returns RemoteResult.Error(remoteError)

                val result = movieRepository.getMovieListBySearch("abc", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is UnknownHostException)
                assertTrue(errorResult.remoteError is NoInternet)
                assertEquals(
                        R.string.no_internet_service,
                        errorResult.remoteError.messageResId
                )
        }

        /**
         * Test a fail response for search with [HttpException]
         */
        @Test
        fun `get search response as HttpException`() = runBlocking {

                val remoteError = HTTPError(
                        HttpException(
                        Response.error<Any>(
                                401, "test".toResponseBody("text/plain".toMediaType())
                        )
                ), code = 401, httpErrorMessage = "Invalid Api Key")
                coEvery { mockMovieRemoteDataSource.getMovieListBySearch(any(), any()) } returns RemoteResult.Error(remoteError)

                val result = movieRepository.getMovieListBySearch("abc", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is HttpException)
                assertTrue(errorResult.remoteError is HTTPError)
                assertEquals(401, (errorResult.remoteError as HTTPError).code)
                assertEquals(
                        "Invalid Api Key",
                        (errorResult.remoteError as HTTPError).httpErrorMessage
                )
        }

        /**
         * Test a fail response for search with [IOException]
         */
        @Test
        fun `get search response as IOException`() = runBlocking {

                val remoteError = IOError(IOException())
                coEvery { mockMovieRemoteDataSource.getMovieListBySearch(any(), any()) } returns RemoteResult.Error(remoteError)

                val result = movieRepository.getMovieListBySearch("abc", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is IOException)
                assertTrue(errorResult.remoteError is IOError)
                assertEquals(R.string.network_io_error, errorResult.remoteError.messageResId)
        }

        /**
         * Test a fail response for search with unknown exception
         */
        @Test
        fun `get search response as Unknown Exception`() = runBlocking {

                val remoteError = Other(Exception())
                coEvery { mockMovieRemoteDataSource.getMovieListBySearch(any(), any()) } returns RemoteResult.Error(remoteError)

                val result = movieRepository.getMovieListBySearch("abc", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is Exception)
                assertTrue(errorResult.remoteError is Other)
                assertEquals(
                        R.string.network_error,
                        errorResult.remoteError.messageResId
                )
        }

}