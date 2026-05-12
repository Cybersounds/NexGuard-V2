package com.cybernexus.nexguard.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import com.cybernexus.nexguard.MainActivity

class ForegroundProtectionService : Service() {

    private val volumePressTimestamps = mutableListOf<Long>()
    private val PRESS_LIMIT = 3
    private val TIME_WINDOW_MS = 2000L

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        Log.d("NexGuard", "Protection Service Started")
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

    fun onVolumePressed() {
        val now = System.currentTimeMillis()
        volumePressTimestamps.add(now)

        // Keep only recent presses within time window
        volumePressTimestamps.removeAll { now - it > TIME_WINDOW_MS }

        Log.d("NexGuard", "Volume press detected: ${volumePressTimestamps.size}/$PRESS_LIMIT")

        if (volumePressTimestamps.size >= PRESS_LIMIT) {
            volumePressTimestamps.clear()
            triggerPanic()
        }
    }

    private fun triggerPanic() {
        Log.d("NexGuard", "PANIC triggered via volume button!")
        PanicService(this).triggerPanic(getDeviceId())
    }

    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
