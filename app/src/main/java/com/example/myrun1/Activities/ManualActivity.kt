package com.example.myrun1.Activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ListView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.myrun1.Database.Exercise
import com.example.myrun1.Database.ExerciseDatabase
import com.example.myrun1.Database.ExerciseDatabaseDao
import com.example.myrun1.Database.ExerciseRepository
import com.example.myrun1.Fragments.DatePickerFragment
import com.example.myrun1.MyDialog
import com.example.myrun1.R
import com.example.myrun1.Fragments.TimePickerFragment
import com.example.myrun1.ViewModel.ExerciseViewModel
import com.example.myrun1.ViewModel.ExerciseViewModelFactory
import com.example.myrun1.ViewModel.ManualEntryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ManualActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private val MANUAL_LIST = arrayOf(
        "Date",
        "Time",
        "Duration",
        "Distance",
        "Calories",
        "Heart Rate",
        "Comment"
    )
    private lateinit var manualList: ListView

    private lateinit var exercise: Exercise
    private lateinit var exerciseDatabase: ExerciseDatabase
    private lateinit var exerciseDatabaseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel

    private var pickedCalendar = Calendar.getInstance()
    private var inputType: Int = 0
    private var activityType: Int = 0

    private lateinit var manualEntryViewModel: ManualEntryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        // Initialize the toolbar
        val toolbar: Toolbar = findViewById(R.id.manual_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Ran_WANG_MyRuns5"

        // Modify the code from RoomDatabaseKotlin
        exerciseDatabase = ExerciseDatabase.getInstance(this)
        exerciseDatabaseDao = exerciseDatabase.ExerciseDatabaseDao
        repository = ExerciseRepository(exerciseDatabaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseViewModel::class.java]

        exercise = Exercise()

        // Get input and activity type
        val intent = intent
        val selectedInputType = intent.getIntExtra("input_types", -1)
        val selectedActivityType = intent.getIntExtra("activity_types", -1)
        println("xd: selectedInputType is: $selectedInputType")
        println("xd: selectedActivityType is: $selectedActivityType")
        inputType = selectedInputType
        activityType = selectedActivityType

        manualEntryViewModel = ViewModelProvider(this)[ManualEntryViewModel::class.java]

        // Modify the code from LayoutKotlin
        manualList = findViewById(R.id.manualListView)
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, MANUAL_LIST)
        manualList.adapter = arrayAdapter

        // Handle click events for ListView items
        manualList.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> showDatePicker()
                1 -> showTimePicker()
                2 -> showMyDialog("duration", MANUAL_LIST[position], "")
                3 -> showMyDialog("distance", MANUAL_LIST[position], "")
                4 -> showMyDialog("calories", MANUAL_LIST[position], "")
                5 -> showMyDialog("heartRate", MANUAL_LIST[position], "")
                6 -> showMyDialog("comment", MANUAL_LIST[position], "How did it go? Notes here.")
            }
        }
    }

    private fun showDatePicker() {
        val newFragment = DatePickerFragment()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    private fun showTimePicker() {
        val newFragment = TimePickerFragment()
        newFragment.show(supportFragmentManager, "timePicker")
    }

    private fun showMyDialog(input: String, title: String, hint: String) {
        val myDialog = MyDialog()
        val bundle = Bundle()

        // Set input type by title
        val inputType = when (title) {
            "Duration", "Distance","Calories", "Heart Rate" ->
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            else ->
                InputType.TYPE_CLASS_TEXT
        }

        // Modify the code from DialogFragmentKotlin
        bundle.putString("input", input)
        bundle.putString("title", title)
        bundle.putString("hint", hint)
        bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.TEST_DIALOG)
        bundle.putInt("input_type", inputType)
        myDialog.arguments = bundle
        myDialog.show(supportFragmentManager, "my dialog")
    }


    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        pickedCalendar.set(Calendar.YEAR, year)
        pickedCalendar.set(Calendar.MONTH, month)
        pickedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        pickedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        pickedCalendar.set(Calendar.MINUTE, minute)
    }

    fun manualOnSaveClick(view: View)
    {
        // Format the date and time
        val tempFormat = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())
        val datetime = tempFormat.format(pickedCalendar.time)

        // Observe the ViewModel values
        val duration = manualEntryViewModel.duration.value ?: 0.0
        val distance = manualEntryViewModel.distance.value ?: 0.0
        val calories = manualEntryViewModel.calories.value ?: 0.0
        val heartRate = manualEntryViewModel.heartRate.value ?: 0.0
        val comment = manualEntryViewModel.comment.value ?: ""

        // Create an Exercise object with the collected data
        val exercise = Exercise(
            inputType = inputType,
            activityType = activityType,
            dateTime = datetime,
            duration = duration,
            distance = distance,
            calories = calories,
            heartRate = heartRate,
            comment = comment
        )

        // Insert the Exercise object into the database using the ViewModel
        exerciseViewModel.insert(exercise)

        exerciseViewModel.getCount().observe(this) { count ->
            Toast.makeText(this, "Entry #${count} created", Toast.LENGTH_SHORT).show()
        }

        // Close the activity after saving
        finish()
    }

    fun manualOnCancelClick(view: View)
    {
        Toast.makeText(this, "Entry discard.", Toast.LENGTH_SHORT).show()
        finish()
    }


}