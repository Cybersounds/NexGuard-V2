package com.cybernexus.nexguard

import android.Manifest
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cybernexus.nexguard.network.ApiClient
import com.cybernexus.nexguard.network.ApiService
import com.cybernexus.nexguard.network.RegisterRequest
import com.cybernexus.nexguard.services.PanicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request location permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1001
        )

        // Generate unique device ID
        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        // Register device on backend
        registerDevice(deviceId)

        try {
            val button = Button(this)
            button.text = "TRIGGER PANIC"

            button.setOnClickListener {
                try {
                    PanicService(this).triggerPanic(deviceId)
                } catch (e: Exception) {
                    Log.e("NexGuard", "Button crash: ${e.message}")
                }
            }

            setContentView(button)
        } catch (e: Exception) {
            Log.e("NexGuard", "Activity crash: ${e.message}")
        }
    }

    private fun registerDevice(deviceId: String) {
        try {
            val api = ApiClient.retrofit.create(ApiService::class.java)
            val request = RegisterRequest(deviceId)
            api.registerDevice(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("NexGuard", "Device registered: ${response.code()}")
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("NexGuard", "Registration failed: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("NexGuard", "Register crash: ${e.message}")
        }
    }
}
