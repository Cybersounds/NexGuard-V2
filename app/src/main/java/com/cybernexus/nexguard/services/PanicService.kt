package com.cybernexus.nexguard.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.cybernexus.nexguard.network.ApiClient
import com.cybernexus.nexguard.network.ApiService
import com.cybernexus.nexguard.network.PanicRequest
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PanicService(private val context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun triggerPanic(deviceId: String) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No permission — send panic without location
            sendPanic(deviceId, null, null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                sendPanic(deviceId, location?.latitude, location?.longitude)
            }
            .addOnFailureListener {
                sendPanic(deviceId, null, null)
            }
    }

    private fun sendPanic(deviceId: String, latitude: Double?, longitude: Double?) {
        try {
            val api = ApiClient.retrofit.create(ApiService::class.java)
            val request = PanicRequest(
                deviceId = deviceId,
                message = "Emergency triggered",
                latitude = latitude,
                longitude = longitude
            )

            api.sendPanic(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("NexGuard", "Panic sent: ${response.code()}")
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("NexGuard", "Panic failed: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("NexGuard", "Crash prevented: ${e.message}")
        }
    }
}
