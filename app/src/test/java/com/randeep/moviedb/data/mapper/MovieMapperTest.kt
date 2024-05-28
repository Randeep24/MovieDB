package com.randeep.moviedb.data.mapper

import com.randeep.moviedb.data.remote.movie.model.MovieSearchResponse
import com.randeep.moviedb.utils.FileReaderUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovieMapperTest {

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


        @Test
        fun `map MovieSearchResponse result to MovieList`() = runBlocking {

                val fileData = FileReaderUtils.readTestResourceFile("search_result.json")
                val searchedListResponse = moshiAdapter.fromJson(fileData)!!

                val movieList = searchedListResponse.asDomainModel()

                assertTrue(movieList.response)

                assertEquals(4871, movieList.totalResults)
                assertEquals(null, movieList.errorMessage)

                assertEquals("The Imitation Game", movieList.movies[1].title)
                assertEquals("2014", movieList.movies[1].year)
                assertEquals("tt2084970", movieList.movies[1].imdbId)

        }

        @Test
        fun `map failed MovieSearchResponse result to MovieList`() = runBlocking {

                val fileData = FileReaderUtils.readTestResourceFile("empty_search_result.json")
                val searchedListResponse = moshiAdapter.fromJson(fileData)!!

                val movieList = searchedListResponse.asDomainModel()

                assertFalse(movieList.response)

                assertEquals(0, movieList.totalResults)
                assertTrue(movieList.errorMessage?.isNotEmpty() == true)

                assertTrue(movieList.movies.isEmpty())

        }
}