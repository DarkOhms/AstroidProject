package com.udacity.asteroidradar.Database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.api.parseAsteroidsJsonResultForPast
import com.udacity.asteroidradar.asDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar

@Dao
interface AsteroidDao{
    @Query("SELECT * FROM asteroid_table ORDER BY closeApproachDate")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids : DatabaseAsteroid)

    //This has been changed to delete asteroids that have a close approach date that is
    //over a month old
    @Query("DELETE FROM asteroid_table WHERE closeApproachDate < date('now', '-1 month')")
    fun deleteOldAsteroids()
}

/*
This all seems like a lot to cache a photo but this is how I've decided
to deal with the requirements.  This table is meant to stay small so
I will keep it to 10, a rather arbitrary number of entries.  I added
an auto increment index to the DatabasePictureOfDay model for sorting and
culling purposes.
 */

@Dao
interface PictureOfDayDao{
    @Query("SELECT * FROM picture_table WHERE mediaType = 'image' ORDER BY id DESC LIMIT 1")
    fun getPictureOfDay(): LiveData<DatabasePictureOfDay?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(picture : DatabasePictureOfDay)

    @Query("SELECT * FROM picture_table WHERE url = :url")
    fun getPictureByUrl(url: String): DatabasePictureOfDay?

    @Transaction
    fun insertNoDuplicate(picture : DatabasePictureOfDay){
        val existingPictureOfDay = getPictureByUrl(picture.url)
        if(existingPictureOfDay == null){
            insert(picture)
        }else{
            //no insert for you!!
        }
    }

    @Query("DELETE FROM picture_table WHERE id > 10")
    fun trimTable()

}

@Database(entities = [DatabaseAsteroid::class,DatabasePictureOfDay::class], version = 2)
abstract class AsteroidDatabase: RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
    abstract val pictureOfDayDao: PictureOfDayDao

    companion object{
        @Volatile
        private var INSTANCE : AsteroidDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AsteroidDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AsteroidDatabase::class.java,
                    "asteroid_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(AsteroidDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance

            }

        }
    }

    private class AsteroidDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            runBlocking {
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.pictureOfDayDao, database.asteroidDao)
                    }

                }
            }
        }

        /**
         * Populate the database in a new coroutine.
         * This adds a default PictureOfTheDay and old asteroids
         */


        suspend fun populateDatabase(pictureOfDayDao: PictureOfDayDao, asteroidDao: AsteroidDao) {

            val defaultPicture = PictureOfDay("image", "default NASA image", "https://api.nasa.gov/assets/img/hero.png").asDatabaseModel()
            pictureOfDayDao.insert(defaultPicture)

            withContext(Dispatchers.IO) {
                    try {
                        val calendar = Calendar.getInstance()
                        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT)
                        val formattedEndDate = dateFormat.format(calendar.time)

                        calendar.add(Calendar.DAY_OF_MONTH, -Constants.DEFAULT_END_DATE_DAYS)
                        val formattedStartDate = dateFormat.format(calendar.time)

                        Log.d("Start Date", formattedStartDate)
                        Log.d("End Date", formattedEndDate)
                        val response = NasaApi.retrofitScalarsService.getAsteroids(
                            formattedStartDate,
                            formattedEndDate,
                            Constants.API_KEY
                        )

                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            val jsonString = responseBody?.string() // Get the response body as a string
                            val jsonObject = JSONObject(jsonString) // Convert the string to a JSONObject
                            Log.d("Success!!!", "Database Pre fetch successful!!!")
                            val asteroids = parseAsteroidsJsonResultForPast(jsonObject)
                            Log.d("Callback", asteroids.size.toString() + " for insertion")
                            withContext(Dispatchers.IO) { asteroidDao.insertAll(*(asteroids.toTypedArray())) }
                        } else {
                            Log.d("Response Failure", response.errorBody().toString())
                        }

                    } catch (e: Exception) {
                        Log.d("Catch Failure", e.toString())
                    }
            }
        }
    }




}
