package com.example.intruder

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

object LocationHelper {
    private const val TAG = "LocationHelper"

    @SuppressLint("MissingPermission")
    fun getLocation(context: Context, callback: (String) -> Unit) {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setWaitForAccurateLocation(true)
            setMaxUpdates(1)
        }.build()

        fused.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location? = result.lastLocation
                if (loc != null) {
                    val locationLink = "https://www.google.com/maps?q=${loc.latitude},${loc.longitude}"
                    Log.d(TAG, "Location obtained: $locationLink")
                    callback(locationLink)
                } else {
                    Log.e(TAG, "Location unavailable")
                    callback("Location unavailable")
                }
            }
        }, Looper.getMainLooper())
    }
}