package com.randeep.moviedb.data.remote.di

import com.randeep.moviedb.data.remote.networkUtil.Constants
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
                        .connectTimeout(Constants.RETROFIT_TIMEOUT, TimeUnit.MILLISECONDS)
                        .writeTimeout(Constants.RETROFIT_TIMEOUT, TimeUnit.MILLISECONDS)
                        .readTimeout(Constants.RETROFIT_TIMEOUT, TimeUnit.MILLISECONDS)
                        .addInterceptor(httpLoggingInterceptor)
                        .build()

        @Singleton
        @Provides
        fun providesNetworkService(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
                Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .client(okHttpClient)
                        .build()
}