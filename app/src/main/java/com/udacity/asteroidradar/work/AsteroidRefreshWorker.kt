package com.udacity.asteroidradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.AsteroidApplication
import com.udacity.asteroidradar.AsteroidRepository
import com.udacity.asteroidradar.Database.AsteroidDatabase
import retrofit2.HttpException

class AsteroidRefreshWorker(appContext: Context, params: WorkerParameters):CoroutineWorker(appContext, params) {
    companion object {
        const val WORK_NAME = "AsteroidRefreshWorker"
    }
    override suspend fun doWork(): Result {
        val application = applicationContext as AsteroidApplication
        val repository = application.repository

        return try{
            repository.networkRefresh()
            repository.asteroidCleanup()
            repository.getNetworkPictureOfTheDay()
            repository.pictureCleanup()
            Result.success()
        }catch (e:HttpException){
            Result.retry()
        }

    }

}