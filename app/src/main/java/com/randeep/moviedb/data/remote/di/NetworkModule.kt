package com.randeep.moviedb.data.remote.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

        private const val RETROFIT_TIMEOUT = 30 * 1000L

        private const val BASE_URL = "https://www.omdbapi.com/"
        private const val API_KEY = "b183a9d3"

        @Singleton
        @Provides
        fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
        }

        @Singleton
        @Provides
        fun providesMoshi(): Moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        @Singleton
        @Provides
        fun providesOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
                OkHttpClient.Builder()
                        .connectTimeout(RETROFIT_TIMEOUT, TimeUnit.MILLISECONDS)
                        .writeTimeout(RETROFIT_TIMEOUT, TimeUnit.MILLISECONDS)
                        .readTimeout(RETROFIT_TIMEOUT, TimeUnit.MILLISECONDS)
                        .addInterceptor(httpLoggingInterceptor)
                        .build()

        @Singleton
        @Provides
        fun providesNetworkService(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
                Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .client(okHttpClient)
                        .build()
}