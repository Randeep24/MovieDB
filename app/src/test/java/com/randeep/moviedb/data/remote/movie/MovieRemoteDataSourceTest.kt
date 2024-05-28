package com.randeep.moviedb.data.remote.movie

import com.randeep.moviedb.R
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MovieRemoteDataSourceTest {

        private val mockMovieApi = mockk<MovieApi>()
        private val remoteDataSource = MovieRemoteDataSourceImpl(mockMovieApi)
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
                val searchedListResponse = moshiAdapter.fromJson(fileData)!!

                coEvery { mockMovieApi.getSearchMovie(any(), any(), any()) } returns searchedListResponse

                val result = remoteDataSource.getMovieListBySearch("game", 1)

                assertTrue(result is RemoteResult.Success)
                assertEquals(searchedListResponse, (result as RemoteResult.Success).value)

                assertTrue(result.value.response.toBoolean())

                val movies = result.value.movies!!

                assertEquals("The Imitation Game", movies[1].title)
                assertEquals("2014", movies[1].year)
                assertEquals("tt2084970", movies[1].imdbId)

                assertEquals("Squid Game", movies[2].title)
                assertEquals("series", movies[2].type)
                assertEquals("2021–", movies[2].year)

        }

        /**
         * Test a successful response for no movie found result
         */
        @Test
        fun `get a response for no movie found result`() = runBlocking {
                val fileData = FileReaderUtils.readTestResourceFile("empty_search_result.json")
                val searchedListResponse = moshiAdapter.fromJson(fileData)!!

                coEvery { mockMovieApi.getSearchMovie(any(), any(), any()) } returns searchedListResponse

                val result = remoteDataSource.getMovieListBySearch("abc", 1)

                assertTrue(result is RemoteResult.Success)
                assertEquals(searchedListResponse, (result as RemoteResult.Success).value)

                assertFalse(result.value.response.toBoolean())
                assertTrue(result.value.movies == null)
                assertEquals("Movie not found!", result.value.errorMessage)
        }

        /**
         * Test a fail response for search with [SocketTimeoutException]
         */
        @Test
        fun `get search response as SocketTimeOutException`() = runBlocking {

                val exception = SocketTimeoutException()
                coEvery { mockMovieApi.getSearchMovie(any(), any(), any()) } answers { throw exception }

                val result = remoteDataSource.getMovieListBySearch("game", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is SocketTimeoutException)
                assertTrue(errorResult.remoteError is TimeOut)
                assertEquals(R.string.network_timed_out, errorResult.remoteError.messageResId)
        }

        /**
         * Test a fail response for search with [UnknownHostException]
         */
        @Test
        fun `get search response as UnknownHostException`() = runBlocking {

                val exception = UnknownHostException()
                coEvery { mockMovieApi.getSearchMovie(any(), any(), any()) } answers { throw exception }

                val result = remoteDataSource.getMovieListBySearch("game", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is UnknownHostException)
                assertTrue(errorResult.remoteError is NoInternet)
                assertEquals(R.string.no_internet_service, errorResult.remoteError.messageResId)
        }

        /**
         * Test a fail response for search with [HttpException]
         */
        @Test
        fun `get search response as HttpException`() = runBlocking {

                val exception = HttpException(
                        Response.error<Any>(
                                401, "test".toResponseBody("text/plain".toMediaType())
                        )
                )
                coEvery { mockMovieApi.getSearchMovie(any(), any(), any()) } answers { throw exception }

                val result = remoteDataSource.getMovieListBySearch("game", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is HttpException)
                assertTrue(errorResult.remoteError is HTTPError)

                val remoteError = errorResult.remoteError as HTTPError

                assertEquals(401, remoteError.code)
        }

        /**
         * Test a fail response for search with [IOException]
         */
        @Test
        fun `get search response as IOException`() = runBlocking {

                val exception = IOException()
                coEvery { mockMovieApi.getSearchMovie(any(), any(), any()) } answers { throw exception }

                val result = remoteDataSource.getMovieListBySearch("game", 1)

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

                val exception = Exception()
                coEvery { mockMovieApi.getSearchMovie(any(), any(), any()) } answers { throw exception }

                val result = remoteDataSource.getMovieListBySearch("game", 1)

                assertTrue(result is RemoteResult.Error)
                val errorResult = result as RemoteResult.Error

                assertTrue(errorResult.remoteError.throwable is Exception)
                assertTrue(errorResult.remoteError is Other)
                assertEquals(R.string.network_error, errorResult.remoteError.messageResId)
        }
}