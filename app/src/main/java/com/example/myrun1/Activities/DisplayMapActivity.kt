package com.example.myrun1.Activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.myrun1.Database.Exercise
import com.example.myrun1.Database.ExerciseDatabase
import com.example.myrun1.Database.ExerciseDatabaseDao
import com.example.myrun1.Database.ExerciseRepository
import com.example.myrun1.R
import com.example.myrun1.ViewModel.ExerciseViewModel
import com.example.myrun1.ViewModel.ExerciseViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class DisplayMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var location: ArrayList<LatLng>

    private lateinit var deleteButton: TextView

    private lateinit var activityView: TextView
    private lateinit var curSpeedView: TextView
    private lateinit var distanceView: TextView
    private lateinit var avgSpeedView: TextView
    private lateinit var climbView: TextView
    private lateinit var caloriesView: TextView

    private lateinit var exercise: ExerciseDatabase
    private lateinit var exerciseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel

    private var position = -1
    private var isMapReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_map)

        val toolbar: Toolbar = findViewById(R.id.mapInformation_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Map"

        activityView = findViewById(R.id.map_activity_type)
        avgSpeedView = findViewById(R.id.map_avg_speed)
        curSpeedView = findViewById(R.id.map_current_speed)
        climbView = findViewById(R.id.map_climb)
        caloriesView = findViewById(R.id.map_calories)
        distanceView = findViewById(R.id.map_distance)

        deleteButton = findViewById(R.id.delete_button)

        location = ArrayList()

        // Modify the code from RoomDatabaseKotlin
        exercise = ExerciseDatabase.getInstance(this)
        exerciseDao = exercise.ExerciseDatabaseDao
        repository = ExerciseRepository(exerciseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseViewModel::class.java]

        // Get the entryIndex to determine which entries to display
        position = intent.getIntExtra("entryIndex", -1)

        observeData()

        // Removing the current Activity from LiveData to
        // ensures that the current page will no longer receive data updates.
        deleteButton.setOnClickListener {
            exerciseViewModel.deleteEntry(position)
            exerciseViewModel.allExerciseLiveData.removeObservers(this)
            finish()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_history)
                as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun observeData() {
        exerciseViewModel.allExerciseLiveData.observe(this, Observer { exerciseList ->
            if (position in exerciseList.indices) {
                val currentEntry = exerciseList[position]
                location = currentEntry.locations
                println("xd: currentEntry.location is: $location")
                if (isMapReady) setupMapAndMarkers(mMap, location)
                updateUIWithData(currentEntry)
            }
        })
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        isMapReady = true

        if (this::location.isInitialized && location.isNotEmpty()) {
            setupMapAndMarkers(mMap, location)
        }
    }

    private fun updateUIWithData(entry: Exercise) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val unitInUse = sharedPreferences.getString("unit_preference", "0")

        val activityType = when (entry.activityType) {
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

        activityView.text = "Type: $activityType"

        val distanceUnit = if (unitInUse == "metric"){
            "Kilometers"
        } else{
            "Miles"
        }

        if (distanceUnit == "Kilometers") {
            avgSpeedView.text = String.format("Avg speed: %.2f km/h", entry.avgSpeed * 3.6)
            curSpeedView.text = "Cur speed: n/a Kilometers"
            distanceView.text = String.format("Distance: %.2f Kilometers", entry.distance * 1.60934)
            climbView.text = String.format("Climb: %.0f Kilometers", entry.climb)
        } else {
            avgSpeedView.text = String.format("Avg speed: %.2f m/h", entry.avgSpeed)
            curSpeedView.text = "Cur speed: n/a Miles"
            distanceView.text = String.format("Distance: %.2f Miles", entry.distance)
            climbView.text = String.format("Climb: %.0f Miles", entry.climb)
        }
        caloriesView.text = String.format("Calories: %.0f", entry.calories)
    }


    private fun setupMapAndMarkers(googleMap: GoogleMap, polylinePoints: ArrayList<LatLng>) {
        if (polylinePoints.isEmpty()) {
            Log.e("DisplayMapActivity", "No points available to display on the map")
            return
        }

        val polylineOptions = PolylineOptions().addAll(polylinePoints).width(5f).color(Color.BLACK)
        googleMap.addPolyline(polylineOptions)

        // Calculate map bound
        val builder = LatLngBounds.Builder()
        for (point in polylinePoints) {
            builder.include(point)
        }
        val bounds = builder.build()

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 500)
        googleMap.animateCamera(cameraUpdate)

        googleMap.addMarker(MarkerOptions().position(polylinePoints.first())
            .title("Start")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
        googleMap.addMarker(MarkerOptions().position(polylinePoints.last())
            .title("End")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
    }
}