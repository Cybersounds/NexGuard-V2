package com.cybernexus.nexguard.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.util.Log
import com.cybernexus.nexguard.services.ForegroundProtectionService

class VolumeButtonReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

        if (event?.action == KeyEvent.ACTION_DOWN &&
            event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        ) {
            Log.d("NexGuard", "Volume DOWN captured by receiver")
            // Signal the service
            val serviceIntent = Intent(context, ForegroundProtectionService::class.java)
            serviceIntent.action = "VOLUME_PRESSED"
            context.startService(serviceIntent)
        }
    }
}
