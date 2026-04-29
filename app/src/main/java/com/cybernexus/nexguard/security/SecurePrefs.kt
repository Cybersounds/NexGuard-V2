package com.cybernexus.nexguard.security

import android.content.Context

class SecurePrefs(context: Context) {

    private val prefs = context.getSharedPreferences("nexguard", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }
}
