package com.udacity.asteroidradar

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.udacity.asteroidradar.main.MainViewModel
import com.udacity.asteroidradar.main.MainViewModelFactory
import com.udacity.asteroidradar.work.AsteroidRefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        backgroundInit()
    }

    private fun backgroundInit() {
        CoroutineScope(Dispatchers.Default).launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {

        //build constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .apply {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    setRequiresDeviceIdle(true)
                }
            }.build()

        //build periodic request for daily work
        val repeatingRequest = PeriodicWorkRequestBuilder<AsteroidRefreshWorker>(
            1,
            TimeUnit.DAYS
        ).setConstraints(constraints)
            .build()

        //call WorkManager and enqueue the work
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            AsteroidRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}
