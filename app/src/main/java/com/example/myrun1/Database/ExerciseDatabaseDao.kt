package com.example.myrun1.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Modify the code from RoomDatabaseKotlin
@Dao
interface ExerciseDatabaseDao
{
    @Insert
    suspend fun insertEntry(exercise: Exercise)

    @Query("SELECT * FROM exercise_table")
    fun getAllEntries(): Flow<List<Exercise>>

    @Query("DELETE FROM exercise_table")
    suspend fun deleteAll()

    @Query("DELETE FROM exercise_table WHERE id = :key")
    suspend fun deleteEntry(key: Long)

    @Query("SELECT COUNT(id) FROM exercise_table")
    suspend fun getCount(): Int

}