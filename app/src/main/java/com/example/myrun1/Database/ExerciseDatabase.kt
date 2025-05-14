package com.example.myrun1.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Modify the code from RoomDatabaseKotlin
@Database(entities = [Exercise::class], version = 1)
@TypeConverters(LatLngConverter::class) // Add your TypeConverter here
abstract class ExerciseDatabase : RoomDatabase() {
    //XD: Room automatically generates implementations of your abstract CommentDatabase class.
    abstract val ExerciseDatabaseDao: ExerciseDatabaseDao

    companion object{
        //The Volatile keyword guarantees visibility of changes to the INSTANCE variable across threads
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        fun getInstance(context: Context) : ExerciseDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        ExerciseDatabase::class.java, "exercise_table").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
