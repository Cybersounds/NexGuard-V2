package com.cybernexus.nexguard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cybernexus.nexguard.services.ForegroundProtectionService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startForegroundService(
            Intent(this, ForegroundProtectionService::class.java)
        )
    }
}
