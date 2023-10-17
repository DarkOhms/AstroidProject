package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/*
Example query
Retrieve a list of Asteroids based on their closest approach date to Earth.
GET https://api.nasa.gov/neo/rest/v1/feed?start_date=START_DATE&end_date=END_DATE&api_key=API_KEY
 */
private const val IMAGE_BASE_URL = "https://api.nasa.gov/planetary/apod?api_key="
private const val ASTEROID_BASE_URL = "https://api.nasa.gov/"


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(ASTEROID_BASE_URL)
    .build()
    interface NasaApiService {
        /**
         * Returns a Coroutine [List] of [Asteroid] which can be fetched with await() if in a Coroutine scope.
         * The @GET annotation indicates that the "feed" endpoint will be requested with the GET
         * HTTP method
         */
        @GET("neo/rest/v1/feed")
        fun getAsteroids(
            @Query("start_date") startDate: String,
            @Query("end_date") endDate: String,
            @Query("api_key") apiKey: String
        ): Call<List<Asteroid>>

        @GET("planetary/apod")
        suspend fun getPictureOfTheDay(@Query("api_key") apiKey: String): PictureOfDay
    }

    /**
     * A public Api object that exposes the lazy-initialized Retrofit service
     */
    object NasaApi {
        val retrofitService : NasaApiService by lazy { retrofit.create(NasaApiService::class.java) }
    }