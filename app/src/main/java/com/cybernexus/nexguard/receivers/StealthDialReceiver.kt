package com.cybernexus.nexguard.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cybernexus.nexguard.MainActivity

class StealthDialReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: return

        if (number.contains("1234")) {
            Log.d("NexGuard", "Secret code detected — launching app")
            resultData = null

            val compName = ComponentName(context, MainActivity::class.java)
            context.packageManager.setComponentEnabledSetting(
                compName,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )

            val launch = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launch)
        }
    }
}
