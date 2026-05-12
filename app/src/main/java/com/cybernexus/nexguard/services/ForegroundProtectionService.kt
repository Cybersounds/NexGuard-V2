package com.cybernexus.nexguard.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat

class ForegroundProtectionService : Service() {

    private val volumePressTimestamps = mutableListOf<Long>()
    private val PRESS_LIMIT = 3
    private val TIME_WINDOW_MS = 2000L

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        Log.d("NexGuard", "Protection Service Started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "VOLUME_PRESSED") {
            onVolumePressed()
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "nexguard_protection"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NexGuard Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Keeps NexGuard active in background" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("NexGuard Active")
            .setContentText("Your protection is running")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun onVolumePressed() {
        val now = System.currentTimeMillis()
        volumePressTimestamps.add(now)
        volumePressTimestamps.removeAll { now - it > TIME_WINDOW_MS }
        Log.d("NexGuard", "Volume press: ${volumePressTimestamps.size}/$PRESS_LIMIT")

        if (volumePressTimestamps.size >= PRESS_LIMIT) {
            volumePressTimestamps.clear()
            triggerSilentPanic()
        }
    }

    private fun triggerSilentPanic() {
        Log.d("NexGuard", "PANIC triggered via volume button!")
        val androidId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
        PanicService(this).triggerPanic(androidId)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
