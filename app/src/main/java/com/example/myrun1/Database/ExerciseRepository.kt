package com.example.myrun1.Database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// Modify the code from RoomDatabaseKotlin
// A Repository manages queries and allows you to use multiple backends.
// In the most common example, the Repository implements the logic for
// deciding whether to fetch data from a network or use results cached in a local database.
class ExerciseRepository(private val exerciseDatabaseDao: ExerciseDatabaseDao) {

    val allEntries: Flow<List<Exercise>> = exerciseDatabaseDao.getAllEntries()

    fun insert(exercise: Exercise){
        CoroutineScope(Dispatchers.IO).launch{
            exerciseDatabaseDao.insertEntry(exercise)
        }
    }

    fun delete(id: Long){
        CoroutineScope(Dispatchers.IO).launch {
            exerciseDatabaseDao.deleteEntry(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(Dispatchers.IO).launch {
            exerciseDatabaseDao.deleteAll()
        }
    }

    suspend fun getCount(): Int {
        return exerciseDatabaseDao.getCount()
    }
}