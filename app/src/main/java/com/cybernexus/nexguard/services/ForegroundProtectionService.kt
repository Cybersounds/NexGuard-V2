package com.cybernexus.nexguard.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ForegroundProtectionService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("NexGuard", "Protection Service Started")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
