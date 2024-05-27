package com.randeep.moviedb.data.di

import com.randeep.moviedb.data.MovieRepositoryImpl
import com.randeep.moviedb.data.MovieRepository
import com.randeep.moviedb.data.remote.movie.MovieRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

        @Provides
        @Singleton
        fun providesMovieRepository(movieRemoteDataSource: MovieRemoteDataSource): MovieRepository =
                MovieRepositoryImpl(movieRemoteDataSource)
}