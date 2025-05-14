package com.example.myrun1.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.example.myrun1.Activities.DisplayInformationActivity
import com.example.myrun1.Activities.DisplayMapActivity
import com.example.myrun1.Database.Exercise
import com.example.myrun1.R


class HistoryListAdapter(private val context: Context, private var entriesList: List<Exercise>) : BaseAdapter(){

    override fun getItem(position: Int): Any {
        return entriesList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return entriesList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, R.layout.layout_history_adapter,null)

        val itemTitle = view.findViewById<TextView>(R.id.entry_title)
        val itemDetail = view.findViewById<TextView>(R.id.entry_detail)

        val currentEntry = entriesList[position]

        // Get units setting
        val unitInUse = getUnitPreference(context)
        println("xd: unitInUse is: $unitInUse")

        // Set entry title
        val entryLabel = getEntryLabel(currentEntry)
        itemTitle.text = entryLabel

        // Set distance and duration
        val distanceText = formatDistance(currentEntry.distance, unitInUse)
        val durationText = formatDuration(currentEntry.duration)
        itemDetail.text = "$distanceText, $durationText"

        // Set onClick to open DisplayInformationActivity
        view.setOnClickListener {
            if(currentEntry.inputType == 0){
            val intent = Intent(context, DisplayInformationActivity::class.java).apply {
                putExtra("entryIndex", position)
            }
            context.startActivity(intent)
            } else {
                val intent = Intent(context, DisplayMapActivity::class.java).apply {
                    putExtra("entryIndex", position)
                }
                context.startActivity(intent)
            }
        }

        return view
    }

    private fun getUnitPreference(context: Context): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString("unit_preference", "0") ?: "0"
    }

    private fun getEntryLabel(entry: Exercise): String {
        val inputType = when (entry.inputType) {
            0 -> "Manual Entry:"
            1 -> "GPS:"
            2 -> "Automatic:"
            else -> "Unknown"
        }

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

        val formattedDateTime = entry.dateTime

        return "$inputType $activityType, $formattedDateTime"
    }

    private fun formatDistance(distance: Double, unit: String): String {
        return when (unit) {
            "metric" -> String.format("%.2f Kilometers", distance * 1.60934)
            "imperial" -> String.format("%.2f Miles", distance)
            else -> "Unknown"
        }
    }

    private fun formatDuration(duration: Double): String {
        val minutes = duration.toInt()
        val seconds = ((duration - minutes) * 60).toInt()

        return when {
            duration >= 1 -> "$minutes mins, $seconds secs"
            else -> "$seconds secs"
        }
    }

    fun replace(newExerciseList: List<Exercise>){
        entriesList = newExerciseList
    }

}