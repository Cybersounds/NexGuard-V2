package com.cybernexus.nexguard

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cybernexus.nexguard.network.*
import com.cybernexus.nexguard.services.ForegroundProtectionService
import com.cybernexus.nexguard.services.PanicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var deviceId: String
    private lateinit var prefs: SharedPreferences
    private var isDark = true

    private val darkBg      = 0xFF0D1117.toInt()
    private val darkSurface = 0xFF161B22.toInt()
    private val darkText    = 0xFFC9D1D9.toInt()
    private val darkSubText = 0xFF8B949E.toInt()
    private val lightBg      = 0xFFF0F4F8.toInt()
    private val lightSurface = 0xFFFFFFFF.toInt()
    private val lightText    = 0xFF0D1117.toInt()
    private val lightSubText = 0xFF57606A.toInt()
    private val accentBlue  = 0xFF1F6FEB.toInt()
    private val panicRed    = 0xFFCC0000.toInt()
    private val white       = 0xFFFFFFFF.toInt()
    private val green       = 0xFF238636.toInt()
    private val errorRed    = 0xFFF85149.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("nexguard_prefs", MODE_PRIVATE)
        isDark = prefs.getBoolean("dark_theme", true)
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Redirect to onboarding if not completed
        if (!prefs.getBoolean("onboarding_done", false)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
	            Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS), 1001)

        registerDevice(deviceId)
        
        // Only start service if location permission already granted
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            startForegroundService(Intent(this, ForegroundProtectionService::class.java))

        }

        buildUI()
    }

    private fun buildUI() {
        val bg      = if (isDark) darkBg      else lightBg
        val surface = if (isDark) darkSurface else lightSurface
        val text    = if (isDark) darkText    else lightText
        val subText = if (isDark) darkSubText else lightSubText

        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 60, 48, 60)
            setBackgroundColor(bg)
        }

        // Header
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 32) }
        }
        val appName = TextView(this).apply {
            this.text = "NEXGUARD"
            textSize = 22f
            setTextColor(accentBlue)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val toggleBtn = TextView(this).apply {
            this.text = if (isDark) "☀ Light" else "🌙 Dark"
            textSize = 13f
            setTextColor(subText)
            setPadding(20, 10, 20, 10)
            background = roundedBg(surface, 20)
            setOnClickListener {
                isDark = !isDark
                prefs.edit().putBoolean("dark_theme", isDark).apply()
                buildUI()
            }
        }
        headerRow.addView(appName)
        headerRow.addView(toggleBtn)
        root.addView(headerRow)

        // Status card
        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)
            background = roundedBg(surface, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 28) }
        }
        statusCard.addView(TextView(this).apply {
            this.text = "● PROTECTION ACTIVE"
            textSize = 13f
            setTextColor(green)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        statusCard.addView(TextView(this).apply {
            this.text = "Device: ${deviceId.take(16)}..."
            textSize = 11f
            setTextColor(subText)
            setPadding(0, 6, 0, 0)
        })
        root.addView(statusCard)

        // Panic button
        val panicStatus = TextView(this).apply {
            this.text = ""
            textSize = 13f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 10, 0, 0) }
        }
        val panicBtn = Button(this).apply {
            this.text = "🚨  TRIGGER PANIC"
            textSize = 20f
            setTextColor(white)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 180
            ).apply { setMargins(0, 0, 0, 10) }
            background = roundedBg(panicRed, 16)
            setOnClickListener {
                try {
                    PanicService(this@MainActivity).triggerPanic(deviceId)
                    panicStatus.text = "✓ Panic alert sent"
                    panicStatus.setTextColor(green)
                } catch (e: Exception) {
                    panicStatus.text = "Error: ${e.message}"
                    panicStatus.setTextColor(errorRed)
                }
            }
        }
        root.addView(panicBtn)
        root.addView(TextView(this).apply {
            this.text = "or hold Volume Down 3× to trigger silently"
            textSize = 11f
            setTextColor(subText)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 36) }
        })
        root.addView(panicStatus)

        // Contacts section
        root.addView(sectionHeader("Emergency Contacts", text))
        val c1n = styledEdit("Contact 1 — Full Name", surface, text, subText)
        val c1p = styledEdit("Contact 1 — Phone (+234...)", surface, text, subText)
        val c2n = styledEdit("Contact 2 — Full Name", surface, text, subText)
        val c2p = styledEdit("Contact 2 — Phone (+234...)", surface, text, subText)
        val c3n = styledEdit("Contact 3 — Full Name", surface, text, subText)
        val c3p = styledEdit("Contact 3 — Phone (+234...)", surface, text, subText)
        listOf(c1n, c1p, c2n, c2p, c3n, c3p).forEach { root.addView(it) }

        val saveStatus = TextView(this).apply {
            this.text = ""
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        val saveBtn = Button(this).apply {
            this.text = "SAVE CONTACTS"
            textSize = 14f
            setTextColor(white)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 8) }
            background = roundedBg(accentBlue, 12)
            setOnClickListener {
                val contacts = mutableListOf<ContactItem>()
                if (c1n.text.isNotEmpty() && c1p.text.isNotEmpty())
                    contacts.add(ContactItem(c1n.text.toString().trim(), c1p.text.toString().trim()))
                if (c2n.text.isNotEmpty() && c2p.text.isNotEmpty())
                    contacts.add(ContactItem(c2n.text.toString().trim(), c2p.text.toString().trim()))
                if (c3n.text.isNotEmpty() && c3p.text.isNotEmpty())
                    contacts.add(ContactItem(c3n.text.toString().trim(), c3p.text.toString().trim()))
                if (contacts.isEmpty()) {
                    saveStatus.text = "Add at least one contact"
                    saveStatus.setTextColor(errorRed)
                    return@setOnClickListener
                }
                saveContacts(deviceId, contacts, saveStatus)
            }
        }
        root.addView(saveBtn)
        root.addView(saveStatus)

        // Stealth Mode — Premium (disabled)
        root.addView(sectionHeader("Stealth Mode", text))
        root.addView(TextView(this).apply {
            this.text = "🔒 Premium feature — coming soon"
            textSize = 12f
            setTextColor(0xFFF0A500.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 12) }
        })
        root.addView(Button(this).apply {
            this.text = "STEALTH MODE — UPGRADE TO UNLOCK"
            textSize = 13f
            setTextColor(0xFF8B949E.toInt())
            isEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 32) }
            background = roundedBg(0xFF21262D.toInt(), 12)
        })

        // Footer
        root.addView(TextView(this).apply {
            this.text = "NexGuard v1.0 — CyberNexus Technologies"
            textSize = 11f
            setTextColor(subText)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        })

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun sectionHeader(label: String, textColor: Int) = TextView(this).apply {
        text = label
        textSize = 15f
        setTextColor(textColor)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 8, 0, 12) }
    }

    private fun styledEdit(hint: String, bg: Int, textColor: Int, hintColor: Int) =
        EditText(this).apply {
            this.hint = hint
            setHintTextColor(hintColor)
            setTextColor(textColor)
            setPadding(24, 20, 24, 20)
            textSize = 14f
            background = roundedBg(bg, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 12) }
        }

    private fun roundedBg(color: Int, radiusDp: Int) =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = radiusDp * resources.displayMetrics.density
        }

    private fun registerDevice(deviceId: String) {
        try {
            val api = ApiClient.retrofit.create(ApiService::class.java)
            api.registerDevice(RegisterRequest(deviceId)).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("NexGuard", "Device registered: ${response.code()}")
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("NexGuard", "Registration failed: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("NexGuard", "Register crash: ${e.message}")
        }
    }

    private fun saveContacts(deviceId: String, contacts: List<ContactItem>, status: TextView) {
        try {
            val api = ApiClient.retrofit.create(ApiService::class.java)
            api.saveContacts(ContactsRequest(deviceId, contacts)).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    runOnUiThread {
                        status.text = "✓ ${contacts.size} contact(s) saved"
                        status.setTextColor(green)
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    runOnUiThread {
                        status.text = "Failed to save contacts"
                        status.setTextColor(errorRed)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("NexGuard", "Contacts crash: ${e.message}")
        }
    }
}
