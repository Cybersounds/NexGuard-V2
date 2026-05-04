package com.cybernexus.nexguard

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cybernexus.nexguard.services.PanicService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val button = Button(this)
            button.text = "TRIGGER PANIC"

            button.setOnClickListener {
                try {
                    PanicService.triggerPanic("device123")
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
