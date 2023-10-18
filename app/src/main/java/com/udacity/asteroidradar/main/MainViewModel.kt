package com.udacity.asteroidradar.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udacity.asteroidradar.Asteroid
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.*

class MainViewModel : ViewModel() {

    private val _asteroids = MutableLiveData<ArrayList<Asteroid>>()

    val asteroids : LiveData<ArrayList<Asteroid>>
        get() = _asteroids

    private val _navigate = MutableLiveData<Boolean>()

    val navigate: LiveData<Boolean>
        get() = _navigate

    private val _selectedAsteroid = MutableLiveData<Asteroid>()

    val selectedAsteroid: LiveData<Asteroid>
        get() = _selectedAsteroid

    private val _pictureOfTheDay = MutableLiveData<PictureOfDay>()

    val pictureOfTheDay : LiveData<PictureOfDay>
        get() = _pictureOfTheDay


    init {
        _navigate.value = false
        getPictureOfTheDay()
        getAsteroids()

    }

    /*
    The majority of this function will be moved to another location, most likely the repository
    and the code for creating the date range will likely have a different implementation.
     */
    private fun getAsteroids()= viewModelScope.launch {
        val calendar = getInstance()
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT)
        val formattedStartDate = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_MONTH, Constants.DEFAULT_END_DATE_DAYS)
        val formattedEndDate = dateFormat.format(calendar.time)

        val call = NasaApi.retrofitScalarsService.getAsteroids(
            formattedStartDate,
            formattedEndDate,
            API_KEY
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonString = responseBody?.string() // Get the response body as a string
                    val jsonObject = JSONObject(jsonString) // Convert the string to a JSONObject
                    Log.d("Success!!!", "NASA API call successful!!!")
                    _asteroids.value = parseAsteroidsJsonResult(jsonObject)

                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle failure case
            }
        })

    }

    private fun getPictureOfTheDay() =
        viewModelScope.launch {
            try {
                _pictureOfTheDay.value = NasaApi.retrofitMoshiService.getPictureOfTheDay(API_KEY)
            }catch (e: Exception){
                 _pictureOfTheDay.value = PictureOfDay("image", "default image", "https://i2-prod.mirror.co.uk/incoming/article9930602.ece/ALTERNATES/s1227b/Ann-Ric-Malaysia-Shortlist-Open-Nature-2017-Sony-World-Photography-Awardsjpeg.jpg")
                Log.d("Picture of the Day Error", e.message.toString())
            }
        }




    fun onAsteroidSelect(asteroid: Asteroid){
        _selectedAsteroid.value = asteroid
        onDetailsNavigate()
    }

    fun onDetailsNavigate(){
        _navigate.value = true

    }
    fun doneNavigating(){
        _navigate.value = false
    }

    fun generateDummyAsteroidList(): List<Asteroid> {
        val dummyAsteroidList = mutableListOf<Asteroid>()

        for (i in 1..15) {
            val asteroid = Asteroid(
                id = i.toLong(),
                codename = "Asteroid $i",
                closeApproachDate = "2023-10-0$i",
                absoluteMagnitude = 5.0 + i.toDouble(),
                estimatedDiameter = 100.0 + i.toDouble(),
                relativeVelocity = 200.0 + i.toDouble(),
                distanceFromEarth = 100000.0 + i.toDouble(),
                isPotentiallyHazardous = i % 2 == 0
            )
            dummyAsteroidList.add(asteroid)
        }
        return dummyAsteroidList
    }
}