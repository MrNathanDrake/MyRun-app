package com.example.myrun1.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ManualEntryViewModel: ViewModel() {
    val duration = MutableLiveData<Double>()
    val distance = MutableLiveData<Double>()
    val calories = MutableLiveData<Double>()
    val heartRate = MutableLiveData<Double>()
    val comment = MutableLiveData<String>()
}