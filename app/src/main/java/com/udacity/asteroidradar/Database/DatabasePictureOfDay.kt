package com.udacity.asteroidradar.Database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.udacity.asteroidradar.PictureOfDay

@Entity(tableName = "picture_table")
data class DatabasePictureOfDay(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @Json(name = "media_type")
    val mediaType: String,
    val title: String,
    val url: String){

    fun asDomainModel(): PictureOfDay{
        return PictureOfDay(
            mediaType = this.mediaType,
            title = this.title,
            url = this.url
        )
    }
}
