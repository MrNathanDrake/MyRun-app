package com.example.myrun1.Database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException

class LatLngConverter {

    // Convert ArrayList<LatLng> to String
    @TypeConverter
    fun fromLatLngList(latLngList: ArrayList<LatLng>?): String? {
        if (latLngList == null) return null
        val jsonArray = JSONArray()
        for (latLng in latLngList) {
            val jsonObject = JSONArray()
            jsonObject.put(latLng.latitude)
            jsonObject.put(latLng.longitude)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    // Convert String to ArrayList<LatLng>
    @TypeConverter
    fun toLatLngList(data: String?): ArrayList<LatLng>? {
        if (data == null) return null
        val latLngList = ArrayList<LatLng>()
        try {
            val jsonArray = JSONArray(data)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONArray(i)
                val lat = jsonObject.getDouble(0)
                val lng = jsonObject.getDouble(1)
                latLngList.add(LatLng(lat, lng))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return latLngList
    }
}