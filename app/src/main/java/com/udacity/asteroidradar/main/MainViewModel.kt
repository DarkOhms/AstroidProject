package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.udacity.asteroidradar.Asteroid
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.AsteroidRepository
import com.udacity.asteroidradar.PictureOfDay
import kotlinx.coroutines.launch
import java.util.Calendar.*

class MainViewModel(repository: AsteroidRepository) : ViewModel() {

    private val repository = repository
    val _asteroids by lazy { repository.filteredAsteroids }

    val asteroids : LiveData<List<Asteroid>>
        get() = _asteroids

    private val _navigate = MutableLiveData<Boolean>()

    val navigate: LiveData<Boolean>
        get() = _navigate

    private val _selectedAsteroid = MutableLiveData<Asteroid>()

    val selectedAsteroid: LiveData<Asteroid>
        get() = _selectedAsteroid

    private val _pictureOfTheDay : LiveData<PictureOfDay?> = repository.pictureOfDay

    val pictureOfTheDay : LiveData<PictureOfDay?>
        get() = _pictureOfTheDay

    init {
        _navigate.value = false
        //this will show all asteroids in the db
        repository.filterLiveData.value = {true}
        //whenever the view model is created, the network refresh will update the database
        //it will also trigger the repository to get the picture of the day from the network
        viewModelScope.launch { repository.getNetworkPictureOfTheDay()}
        viewModelScope.launch { repository.networkRefresh()}

    }

    /*
    The majority of this function will be moved to another location, most likely the repository
    and the code for creating the date range will likely have a different implementation.
     */

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

    fun updateFilter(filter:(Asteroid) -> Boolean){
        repository.updateFilter(filter)

    }

}
class MainViewModelFactory(private val repository: AsteroidRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}