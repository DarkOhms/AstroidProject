package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udacity.asteroidradar.Asteroid
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

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

    init {
        _asteroids.value = generateDummyAsteroidList()
        _navigate.value = false
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