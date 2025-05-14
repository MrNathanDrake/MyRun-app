package com.example.myrun1.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myrun1.Database.Exercise
import com.example.myrun1.Database.ExerciseDatabase
import com.example.myrun1.Database.ExerciseDatabaseDao
import com.example.myrun1.Database.ExerciseRepository
import com.example.myrun1.R
import com.example.myrun1.ViewModel.ExerciseViewModel
import com.example.myrun1.ViewModel.ExerciseViewModelFactory

class HistoryFragment : Fragment() {

    private lateinit var myListView: ListView

    private lateinit var arrayList: ArrayList<Exercise>
    private lateinit var arrayAdapter: HistoryListAdapter

    // Reference: The following DB codes are learned and derived from lecture tutorial
    private lateinit var exercise: ExerciseDatabase
    private lateinit var exerciseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        myListView = view.findViewById(R.id.history_list)
        arrayList = ArrayList()

        arrayAdapter = HistoryListAdapter(requireActivity(), arrayList)
        myListView.adapter = arrayAdapter

        exercise = ExerciseDatabase.getInstance(requireActivity())
        exerciseDao = exercise.ExerciseDatabaseDao
        repository = ExerciseRepository(exerciseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ExerciseViewModel::class.java]

        exerciseViewModel.allExerciseLiveData.observe(requireActivity(), Observer { it ->
                arrayAdapter.replace(it)
                arrayAdapter.notifyDataSetChanged()
            })

        return view
    }

    override fun onResume() {
        super.onResume()
        // Force the adapter to refresh the list
        arrayAdapter.notifyDataSetChanged()
    }
}