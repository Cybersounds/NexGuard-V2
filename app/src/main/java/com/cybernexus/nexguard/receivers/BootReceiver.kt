package com.cybernexus.nexguard.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cybernexus.nexguard.services.ForegroundProtectionService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, ForegroundProtectionService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
