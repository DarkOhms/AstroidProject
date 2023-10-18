package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query


/*
Example query
Retrieve a list of Asteroids based on their closest approach date to Earth.
GET https://api.nasa.gov/neo/rest/v1/feed?start_date=START_DATE&end_date=END_DATE&api_key=API_KEY
 */
private const val ASTEROID_BASE_URL = "https://api.nasa.gov/"


private val logging = HttpLoggingInterceptor().apply {
    this.level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofitMoshi = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(ASTEROID_BASE_URL)
    .client(client)
    .build()

private val retrofitScalars = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(ASTEROID_BASE_URL)
    .client(client)
    .build()
    interface NasaApiService {

        @GET("neo/rest/v1/feed")
        fun getAsteroids(
            @Query("start_date") startDate: String,
            @Query("end_date") endDate: String,
            @Query("api_key") apiKey: String
        ): Call<ResponseBody>

        @GET("planetary/apod")
        suspend fun getPictureOfTheDay(@Query("api_key") apiKey: String): PictureOfDay
    }

    /**
     * A public Api object that exposes the lazy-initialized Retrofit service
     */
    object NasaApi {
        val retrofitMoshiService : NasaApiService by lazy { retrofitMoshi.create(NasaApiService::class.java) }
        val retrofitScalarsService: NasaApiService by lazy { retrofitScalars.create(NasaApiService::class.java)}
    }