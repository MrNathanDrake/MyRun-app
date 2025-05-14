package com.example.myrun1.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.myrun1.Database.Exercise
import com.example.myrun1.Database.ExerciseDatabase
import com.example.myrun1.Database.ExerciseDatabaseDao
import com.example.myrun1.Database.ExerciseRepository
import com.example.myrun1.Services.MapService
import com.example.myrun1.R
import com.example.myrun1.ViewModel.ExerciseViewModel
import com.example.myrun1.ViewModel.ExerciseViewModelFactory
import com.example.myrun1.ViewModel.MyMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener{

    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: Polyline
    private lateinit var lastLocationMarker: Marker

    private lateinit var exercise: Exercise
    private lateinit var exerciseDatabase: ExerciseDatabase
    private lateinit var exerciseDatabaseDao: ExerciseDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var exerciseViewModel: ExerciseViewModel

    private lateinit var mapViewModel: MyMapViewModel
    private val BIND_STATUS_KEY = "bind_status_key"
    private lateinit var appContext: Context
    private var isBind = false

    private lateinit var activityView: TextView
    private lateinit var curSpeedView: TextView
    private lateinit var distanceView: TextView
    private lateinit var avgSpeedView: TextView
    private lateinit var climbView: TextView
    private lateinit var caloriesView: TextView

    private var inputType: Int = 0
    private var activityType: Int = 0
    private lateinit var activityTypeText: String
    private var dateTime: String = ""

    private var climb: Double = 0.0
    private var duration: Double = 0.0
    private lateinit var locationList: ArrayList<LatLng>
    private var distanceUnit: String = ""
    private var lastLocation: Location? = null

    private var startTime: Long = 0
    private var endTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize the toolbar
        val toolbar: Toolbar = findViewById(R.id.map_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Map"

        // Modify the code from RoomDatabaseKotlin
        exerciseDatabase = ExerciseDatabase.getInstance(this)
        exerciseDatabaseDao = exerciseDatabase.ExerciseDatabaseDao
        repository = ExerciseRepository(exerciseDatabaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseViewModel::class.java]

        mapViewModel = ViewModelProvider(this)[MyMapViewModel::class.java]

        // Restore values from savedInstanceState after initializing ViewModel
        if (savedInstanceState != null) {
            mapViewModel.updateSpeed(savedInstanceState.getDouble("curSpeed"), reset = true)
            mapViewModel.updateDistance(savedInstanceState.getDouble("totalDistance"), reset = true)
            mapViewModel.updateCalories(savedInstanceState.getDouble("calories"), reset = true)
            println("The value of isBind is " + savedInstanceState.getBoolean(BIND_STATUS_KEY))
            isBind = savedInstanceState.getBoolean(BIND_STATUS_KEY)
        }

        locationList = ArrayList()

        startTime = System.currentTimeMillis() / 1000

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        activityView = findViewById(R.id.map_activity_type)
        curSpeedView = findViewById(R.id.map_current_speed)
        distanceView = findViewById(R.id.map_distance)
        avgSpeedView = findViewById(R.id.map_avg_speed)
        climbView = findViewById(R.id.map_climb)
        caloriesView = findViewById(R.id.map_calories)

        // Get input and activity type
        val intent = intent
        val selectedInputType = intent.getIntExtra("input_types", -1)
        val selectedActivityType = intent.getIntExtra("activity_types", -1)
        println("xd: selectedInputType is: $selectedInputType")
        println("xd: selectedActivityType is: $selectedActivityType")
        inputType = selectedInputType

        if(inputType == 1){
            activityType = selectedActivityType

            activityTypeText = when (activityType) {
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
        }
        else if(inputType == 2){
            activityType = 14
            activityTypeText = "Unknown"
        }


        val tempFormat = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())
        dateTime = tempFormat.format(Calendar.getInstance().time)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val unitSelected  = sharedPreferences.getString("unit_preference", "0").toString()
        println("xd: unitSelected is: $unitSelected")

        distanceUnit = if (unitSelected == "metric"){
            "Kilometers"
        } else{
            "Miles"
        }
    }

    // Modify the code from I_am_here_map_Kotlin
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        polylineOptions = PolylineOptions()
        polylineOptions.width(5f).color(Color.BLACK)
        markerOptions = MarkerOptions()

        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
    }

    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

//            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//            if (location != null)
//                onLocationChanged(location)
//
//            //minDistanceM: predict the position
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            location?.let { onLocationChanged(it) }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)

        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        println("debug: onlocationchanged() ${location.latitude} ${location.longitude}")
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)

        locationList.add(latLng)
        polylineOptions.add(latLng)
        polylines = mMap.addPolyline(polylineOptions)

        // Move the last location marker
        if (::lastLocationMarker.isInitialized) {
            lastLocationMarker.position = latLng
        } else {
            // If this is the first time, initialize the marker
            lastLocationMarker = mMap.addMarker(
                MarkerOptions().position(latLng).icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            )!!
        }

        // Add the on changing speed to the text view
        val speed = location.speedAccuracyMetersPerSecond
        println("xd: curSpeed is: $speed")

        val distanceBetween = lastLocation?.distanceTo(location) ?: 0f
        mapViewModel.updateSpeed(speed.toDouble())
        mapViewModel.updateDistance(distanceBetween.toDouble())

        // Calculate calories and update
        val caloriesBurned = calculateCaloriesByDistance(distanceBetween.toDouble(), activityTypeText, distanceUnit)
        mapViewModel.updateCalories(caloriesBurned)

        // Update the last location to the current location
        lastLocation = location

        updateDisplayTexts()

        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            mMap.addMarker(markerOptions)
            polylineOptions.add(latLng)
            mapCentered = true
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateDisplayTexts() {
        if (inputType == 1){
            activityView.text = "Type: $activityTypeText"
        } else {
            activityView.text = "Type: Unknown"
            activityType = 14

            mapViewModel.activityType.observe(this) { activityTypeString ->
                println("xddd: activityView is changed to $activityTypeString")
                activityView.text = "Type: $activityTypeString"
                when (activityTypeString) {
                    "Running" -> {
                        activityType = 0
                    }
                    "Walking" -> {
                        activityType = 1
                    }
                    "Standing" -> {
                        activityType = 2
                    }
                }
            }
        }

        if (distanceUnit == "Kilometers") {
            // Format for Kilometers
            mapViewModel.curSpeed.observe(this) { speed ->
                curSpeedView.text = String.format("Cur speed: %.2f km/h", speed * 3.6)
            }
            mapViewModel.avgSpeed.observe(this) { avgSpeed ->
                avgSpeedView.text = String.format("Avg speed: %.2f km/h", avgSpeed * 3.6)
            }
            mapViewModel.totalDistance.observe(this) { distance ->
                distanceView.text = String.format("Distance: %.2f Kilometers", distance / 1000)
            }
            mapViewModel.calories.observe(this) { calories ->
                caloriesView.text = String.format("Calories: %.0f", calories)
            }
            val climbText = String.format("%.0f Kilometers", climb)
            climbView.text = "Climb: $climbText"
        } else {
            // Format for Miles
            mapViewModel.curSpeed.observe(this) { speed ->
                curSpeedView.text = String.format("Cur speed: %.2f m/h", speed)
            }
            mapViewModel.avgSpeed.observe(this) { avgSpeed ->
                avgSpeedView.text = String.format("Avg speed: %.2f m/h", avgSpeed)
            }
            mapViewModel.totalDistance.observe(this) { distance ->
                distanceView.text = String.format("Distance: %.2f Miles", distance / 1000)
            }
            mapViewModel.calories.observe(this) { calories ->
                caloriesView.text = String.format("Calories: %.0f", calories)
            }

            val climbText = String.format("%.0f Miles", climb)
            climbView.text = "Climb: $climbText"
        }

    }

    private fun calculateCaloriesByDistance(distance: Double, activityType: String, unit: String): Double {
        val distanceKm = if (unit == "Miles")
            distance / 1000 else distance

        // Choose calorie factor based on type of activity
        val calorieCoefficient = when (activityType) {
            "Running" -> 2
            "Cycling" -> 2
            else -> 1
        }

        // Calculating calorie burn
        return distanceKm * calorieCoefficient
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 26) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else{
            initLocationManager()
            startService()
            bindService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initLocationManager()
        }
    }

    fun mapOnSaveClick(view: View)
    {
        stopService()

        endTime = System.currentTimeMillis() /1000
        val durationSeconds = endTime - startTime
        val durationMinutes = durationSeconds / 60.0
        duration = durationMinutes

        locationManager.removeUpdates(this)

        val avgSpeed = mapViewModel.avgSpeed.value ?: 0.0
        val distance = mapViewModel.totalDistance.value ?: 0.0
        val calories = mapViewModel.calories.value ?: 0.0

        val exercise = Exercise(
            inputType = inputType,
            activityType = activityType,
            dateTime = dateTime,
            avgSpeed = avgSpeed,
            climb = climb,
            calories = calories,
            distance = distance/1000,
            locations = locationList,
            duration = duration,
        )

        // Insert the Exercise object into the database using the ViewModel
        exerciseViewModel.insert(exercise)

        finish()
    }

    fun mapOnCancelClick(view: View)
    {
        stopService()
        locationManager.removeUpdates(this)

        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BIND_STATUS_KEY, isBind)
        outState.putLong("startTime", startTime)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        startTime = savedInstanceState.getLong("startTime", System.currentTimeMillis() / 1000)
    }

    private fun startService(){
        val serviceIntent = Intent(this, MapService::class.java)
        this.applicationContext.startService(serviceIntent)
    }

    private fun stopService(){
        unBindService()
        val serviceIntent = Intent(this, MapService::class.java)
        this.stopService(serviceIntent)
    }

    private fun bindService(){
        if (!isBind) {
            val serviceIntent = Intent(this, MapService::class.java)
            this.applicationContext.bindService(serviceIntent,mapViewModel ,Context.BIND_AUTO_CREATE)
            isBind = true
        }
    }

    private fun unBindService(){
        if (isBind) {
            this.applicationContext.unbindService(mapViewModel)
            isBind = false
        }
    }

}