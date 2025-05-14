package com.example.myrun1.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import com.example.myrun1.Activities.ManualActivity
import com.example.myrun1.Activities.MapActivity
import com.example.myrun1.Services.MapService
import com.example.myrun1.R

class StartFragment : Fragment() {
    private lateinit var inputTypeSpinner : Spinner
    private lateinit var activityTypeSpinner: Spinner
    private lateinit var startButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        inputTypeSpinner = view.findViewById(R.id.input_type_spinner)
        activityTypeSpinner = view.findViewById(R.id.activity_type_spinner)
        startButton = view.findViewById(R.id.start_button)

        startButton.setOnClickListener(){
            val inputType = inputTypeSpinner.selectedItem.toString()
            val inputTypeNum = inputTypeSpinner.selectedItemPosition
            val activityType = activityTypeSpinner.selectedItemPosition

            var intent: Intent? = null

            if(inputType == "Manual Entry") {
                intent = Intent(context, ManualActivity().javaClass)
            }
            else if (inputType == "GPS" || inputType == "Automatic"){
                intent = Intent(context, MapActivity().javaClass)
            }

            // Pass the user-selected choices to the next activity
            if (intent != null) {
                intent.putExtra("input_types", inputTypeNum)
                intent.putExtra("activity_types", activityType)
            }

            startActivity(intent)
        }

        return view
    }

}