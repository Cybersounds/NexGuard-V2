package com.cybernexus.nexguard

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cybernexus.nexguard.services.PanicService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request location permission from user
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1001
        )

        try {
            val button = Button(this)
            button.text = "TRIGGER PANIC"

            button.setOnClickListener {
                try {
                    PanicService(this).triggerPanic("device123")
                } catch (e: Exception) {
                    Log.e("NexGuard", "Button crash: ${e.message}")
                }
            }

            setContentView(button)
        } catch (e: Exception) {
            Log.e("NexGuard", "Activity crash: ${e.message}")
        }
    }
}
