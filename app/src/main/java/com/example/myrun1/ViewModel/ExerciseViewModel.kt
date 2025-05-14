package com.example.myrun1.ViewModel

import android.app.Application
import androidx.lifecycle.*
import com.example.myrun1.Database.Exercise
import com.example.myrun1.Database.ExerciseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


// Modify the code from RoomDatabaseKotlin
class ExerciseViewModel(private val repository: ExerciseRepository) : ViewModel() {
    val allExerciseLiveData: LiveData<List<Exercise>> = repository.allEntries.asLiveData()

    fun insert(exercise: Exercise) {
        repository.insert(exercise)
    }

    fun deleteFirst(){
        val exerciseList = allExerciseLiveData.value
        if (exerciseList != null && exerciseList.size > 0){
            val id = exerciseList[0].id
            repository.delete(id)
        }
    }

    fun deleteEntry(position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val exerciseList = allExerciseLiveData.value
            if (exerciseList != null && exerciseList.size > 0) {
                val id = exerciseList[position].id
                repository.delete(id)
            }
        }
    }

    fun deleteAll(){
        val exerciseList = allExerciseLiveData.value
        if (exerciseList != null && exerciseList.size > 0)
            repository.deleteAll()
    }

    fun getCount(): LiveData<Int> {
        val count = MutableLiveData<Int>()
        viewModelScope.launch {
            count.postValue(repository.getCount())
        }
        return count
    }
}

class ExerciseViewModelFactory (private val repository: ExerciseRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        //create() creates a new instance of the modelClass, which is CommentViewModel in this case.
        if(modelClass.isAssignableFrom(ExerciseViewModel::class.java))
            return ExerciseViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}