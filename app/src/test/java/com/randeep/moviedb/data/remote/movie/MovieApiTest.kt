package com.randeep.moviedb.data.remote.movie

import com.randeep.moviedb.data.remote.networkUtil.Constants
import com.randeep.moviedb.utils.FileReaderUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.Exception
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class MovieApiTest {

        private val mockWebServer = MockWebServer()
        private lateinit var movieApi: MovieApi

        @Before
        fun setup() {
                val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()

                val okHttpClient = OkHttpClient.Builder()
                        .readTimeout(Constants.RETROFIT_TIMEOUT, TimeUnit.SECONDS)
                        .build()
                movieApi = Retrofit.Builder()
                        .baseUrl(mockWebServer.url("/"))
                        .client(okHttpClient)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build()
                        .create(MovieApi::class.java)

        }

        @After
        fun stop() {
                mockWebServer.shutdown()
        }

        /**
         * Test a successful response for searched movie list
         */
        @Test
        fun `fetch searched movie list`() = runBlocking {
                val mockResponse = MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .setBody(FileReaderUtils.readTestResourceFile("search_result.json"))
                mockWebServer.enqueue(mockResponse)

                val result = movieApi.getSearchMovie("game",1, "abc")
                Assert.assertTrue(result.response.toBoolean())
                Assert.assertTrue(result.movies?.isNotEmpty() == true)
                val movieList = result.movies!!

                Assert.assertEquals(10, movieList.size)
                Assert.assertEquals("Game of Thrones", movieList[0].title)
                Assert.assertEquals("tt0944947", movieList[0].imdbId)
                Assert.assertEquals("series", movieList[0].type)

                Assert.assertEquals("Gerald's Game", movieList[9].title)
                Assert.assertEquals("movie", movieList[9].type)
                Assert.assertEquals("2017", movieList[9].year)

        }

        /**
         * Test a successful response for no movie found result
         */
        @Test
        fun `fetch no movie found result`() = runBlocking {
                val mockResponse = MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .setBody(FileReaderUtils.readTestResourceFile("empty_search_result.json"))
                mockWebServer.enqueue(mockResponse)

                val result = movieApi.getSearchMovie("hfkfka", 1, "abc")

                Assert.assertFalse(result.response.toBoolean())
                Assert.assertTrue(result.movies == null)
                Assert.assertEquals("Movie not found!", result.errorMessage)
        }

        /**
         * Test a response for invalid api key result
         */
        @Test
        fun `fetch invalid api key result`() = runBlocking {

                val mockResponse = MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                        .setBody("{\"Response\":\"False\",\"Error\":\"Invalid API key!\"}")
                mockWebServer.enqueue(mockResponse)

                val exception = try {
                        movieApi.getSearchMovie("try", 1, "abcd")
                } catch (e: Exception){
                        e
                }

                Assert.assertTrue(exception is HttpException)
                exception as HttpException
                Assert.assertEquals(401, exception.code())
        }
}