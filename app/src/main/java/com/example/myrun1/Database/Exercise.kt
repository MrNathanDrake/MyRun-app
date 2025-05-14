package com.example.myrun1.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

// Modify the code from RoomDatabaseKotlin
@Entity(tableName = "exercise_table")
data class Exercise (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "input_type")
    var inputType: Int = 0,

    @ColumnInfo(name = "activity_type")
    var activityType: Int = 0,

    @ColumnInfo(name = "date_time")
    var dateTime: String = "",

    @ColumnInfo(name = "duration")
    var duration: Double = 0.0,

    @ColumnInfo(name = "distance")
    var distance: Double = 0.0,

    @ColumnInfo(name = "average_pace")
    var avgPace: Double = 0.0,

    @ColumnInfo(name = "average_speed")
    var avgSpeed: Double = 0.0,

    @ColumnInfo(name = "calories")
    var calories: Double = 0.0,

    @ColumnInfo(name = "climb")
    var climb: Double = 0.0,

    @ColumnInfo(name = "heart_rate")
    var heartRate: Double = 0.0,

    @ColumnInfo(name = "comment")
    var comment: String = "",

    @ColumnInfo(name = "locations")
    var locations: ArrayList<LatLng> = ArrayList()
)