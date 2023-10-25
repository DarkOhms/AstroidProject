package com.udacity.asteroidradar

import android.app.Application
import com.udacity.asteroidradar.Database.AsteroidDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/*
This will provide scope and encapsulate the database and repository
 */
class AsteroidApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy{AsteroidDatabase.getDatabase(this, applicationScope)}

    val repository by lazy{AsteroidRepository(database)}

}