package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udacity.asteroidradar.Asteroid
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.NasaApiService
import kotlinx.coroutines.launch
import java.lang.Exception

class MainViewModel : ViewModel() {

    private val _asteroids = MutableLiveData<List<Asteroid>>()

    val asteroids : LiveData<List<Asteroid>>
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
        _asteroids.value = generateDummyAsteroidList()
        _navigate.value = false
        getPictureOfTheDay()

    }

    private fun getPictureOfTheDay() =
        viewModelScope.launch {
            try {
                _pictureOfTheDay.value = NasaApi.retrofitService.getPictureOfTheDay(API_KEY)
            }catch (e: Exception){
                 _pictureOfTheDay.value = PictureOfDay("image", "default image", "https://i2-prod.mirror.co.uk/incoming/article9930602.ece/ALTERNATES/s1227b/Ann-Ric-Malaysia-Shortlist-Open-Nature-2017-Sony-World-Photography-Awardsjpeg.jpg")
                //TO DO handle error
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