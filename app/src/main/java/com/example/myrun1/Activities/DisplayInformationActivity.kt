package com.example.myrun1.Activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.myrun1.Database.ExerciseDatabase
import com.example.myrun1.Database.ExerciseDatabaseDao
import com.example.myrun1.Database.ExerciseRepository
import com.example.myrun1.R
import com.example.myrun1.ViewModel.ExerciseViewModel
import com.example.myrun1.ViewModel.ExerciseViewModelFactory

class DisplayInformationActivity : AppCompatActivity() {

    private lateinit var deleteButton: TextView

    private lateinit var inputTypeText: EditText
    private lateinit var activityTypeText: EditText
    private lateinit var dateTimeText: EditText
    private lateinit var durationText: EditText
    private lateinit var distanceText: EditText
    private lateinit var caloriesText: EditText
    private lateinit var heartRateText: EditText

    private lateinit var exercise: ExerciseDatabase
    private lateinit var exerciseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_information)

        val toolbar: Toolbar = findViewById(R.id.information_toolbar)
        setSupportActionBar(toolbar)

        inputTypeText = findViewById(R.id.input_type)
        activityTypeText = findViewById(R.id.activity_type)
        dateTimeText = findViewById(R.id.date_time)
        durationText = findViewById(R.id.duration)
        distanceText = findViewById(R.id.distance)
        caloriesText = findViewById(R.id.calories)
        heartRateText = findViewById(R.id.heart_rate)

        deleteButton = findViewById(R.id.delete_button)

        // Modify the code from RoomDatabaseKotlin
        exercise = ExerciseDatabase.getInstance(this)
        exerciseDao = exercise.ExerciseDatabaseDao
        repository = ExerciseRepository(exerciseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseViewModel::class.java]

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val unitInUse = sharedPreferences.getString("unit_preference", "0")

        // Get the entryIndex to determine which entries to display
        val position = intent.getIntExtra("entryIndex", -1)

        exerciseViewModel.allExerciseLiveData.observe(this, Observer
        {
            exerciseList ->
            val currentEntry = exerciseList[position]

            val inputType = when (currentEntry.inputType) {
                0 -> "Manual Entry"
                1 -> "GPS"
                2 -> "Automatic"
                else -> "Unknown"
            }
            inputTypeText.setText(inputType)

            val activityType = when (currentEntry.activityType) {
                0 -> "Running"
                1 -> "Walking"
                2 -> "Standing"
                3 -> "Cycling"
                4 -> "Hiking"
                5 -> "Downhill Skiing"
                6 -> "Cross-Country Skiing"
                7 -> "Snowboarding"
                8 -> "Skating"
                9 -> "Swimming"
                10 -> "Mountain Biking"
                11 -> "Wheelchair"
                12 -> "Elliptical"
                13 -> "Other"
                else -> "Unknown"
            }
            activityTypeText.setText(activityType)

            val formattedDateTime = currentEntry.dateTime
            dateTimeText.setText(formattedDateTime)

            val distance = when (unitInUse) {
                "metric" -> String.format("%.2f Kilometers", currentEntry.distance * 1.60934)
                "imperial" -> String.format("%.2f Miles", currentEntry.distance)
                else -> "Unknown"
            }
            distanceText.setText(distance)

            // Format the duration to show as “X mins, Y secs” or “Y secs
            val duration = currentEntry.duration
            val minutes = duration.toInt()
            val seconds = ((duration - minutes) * 60).toInt()
            val formattedDuration = if (duration >= 1)
                "$minutes mins, $seconds secs" else "$seconds secs"
            durationText.setText(formattedDuration)

            val calories = "${currentEntry.calories} cals"
            caloriesText.setText(calories)

            val heartRate = "${currentEntry.heartRate} bpm"
            heartRateText.setText(heartRate)
        })


        // Removing the current Activity from LiveData to
        // ensures that the current page will no longer receive data updates.
        deleteButton.setOnClickListener {
            exerciseViewModel.deleteEntry(position)
            exerciseViewModel.allExerciseLiveData.removeObservers(this)
            finish()
        }
    }
}