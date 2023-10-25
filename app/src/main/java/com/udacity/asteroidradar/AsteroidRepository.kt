package com.udacity.asteroidradar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.udacity.asteroidradar.Database.AsteroidDatabase
import com.udacity.asteroidradar.Database.DatabaseAsteroid
import com.udacity.asteroidradar.Database.asDomainModel
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar

class AsteroidRepository(private val db:AsteroidDatabase){


    val asteroids: LiveData<List<Asteroid>> = db.asteroidDao.getAsteroids().map { it.asDomainModel() }

    val pictureOfTheDay : LiveData<PictureOfDay> = db.pictureOfDayDao.getPictureOfDay().map { it.asDomainModel() }

    suspend fun networkRefresh (){
        withContext(IO) {
            try {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT)
                val formattedStartDate = dateFormat.format(calendar.time)

                calendar.add(Calendar.DAY_OF_MONTH, Constants.DEFAULT_END_DATE_DAYS)
                val formattedEndDate = dateFormat.format(calendar.time)

                val response = NasaApi.retrofitScalarsService.getAsteroids(
                    formattedStartDate,
                    formattedEndDate,
                    Constants.API_KEY
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonString = responseBody?.string() // Get the response body as a string
                    val jsonObject = JSONObject(jsonString) // Convert the string to a JSONObject
                    Log.d("Success!!!", "NASA API call successful!!!")
                    val asteroids = parseAsteroidsJsonResult(jsonObject)
                    withContext(IO) { insertAsteroids(asteroids) }
                } else {
                    Log.d("Response Failure", response.errorBody().toString())
                }

            } catch (e: Exception) {
                Log.d("Catch Failure", e.toString())
            }
        }
    }

    private fun insertAsteroids(asteroids: ArrayList<DatabaseAsteroid>){
        db.asteroidDao.insertAll(*(asteroids.toTypedArray()))
    }

    private fun insertPictureOfDay(pictureOfDay: PictureOfDay){
        db.pictureOfDayDao.insertNoDuplicate(pictureOfDay.asDatabaseModel())
    }

    suspend fun getNetworkPictureOfTheDay() {
        withContext(IO) {
            val pictureOfDay : PictureOfDay
            try {//
                pictureOfDay = NasaApi.retrofitMoshiService.getPictureOfTheDay(Constants.API_KEY)
                insertPictureOfDay(pictureOfDay)
            } catch (e: java.lang.Exception) {
                Log.d("Picture of the Day Error", e.message.toString())
            }
        }
    }

    suspend fun asteroidCleanup(){
        db.asteroidDao.deleteOldAsteroids()
    }

    suspend fun pictureCleanup(){
        db.pictureOfDayDao.trimTable()
    }

}