package com.example.myrun1.ViewModel

import android.content.ComponentName
import android.content.ServiceConnection
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myrun1.Services.MapService

class MyMapViewModel : ViewModel(), ServiceConnection {

    private var myMessageHandler: MyMessageHandler = MyMessageHandler(Looper.getMainLooper())

    val curSpeed = MutableLiveData<Double>()
    val avgSpeed = MutableLiveData<Double>()
    val totalDistance = MutableLiveData<Double>()
    val calories = MutableLiveData<Double>()
    private var speedCount: Int = 0
    private var totalSpeedSum: Double = 0.0
    var lastLocation: Location? = null

    private var _activityType = MutableLiveData<String>()
    val activityType: LiveData<String> get()
    {
        return _activityType
    }

    fun updateSpeed(speed: Double, reset: Boolean = false) {
        if (reset) {
            return
        } else {
            speedCount++
            totalSpeedSum += speed
        }
        curSpeed.value = speed
        avgSpeed.value = totalSpeedSum / speedCount
    }

    fun updateDistance(distance: Double, reset: Boolean = false) {
        if (reset) {
            return
        } else {
            totalDistance.value = (totalDistance.value ?: 0.0) + distance
        }
    }

    fun updateCalories(caloriesToAdd: Double, reset: Boolean = false) {
        if (reset) {
            return
        } else {
            calories.value = (calories.value ?: 0.0) + caloriesToAdd
        }
    }

    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        println("debug: onServiceConnected() called")
        val tempBinder = iBinder as MapService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        println("debug: onServiceDisconnected() called")
    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MapService.CLASSIFY_MSG_INT_VALUE)
            {
                _activityType.value = msg.data.getString("CLASSIFY_KEY")
            }
        }
    }
}