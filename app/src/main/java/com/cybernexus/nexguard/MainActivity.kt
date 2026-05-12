kpackage com.cybernexus.nexguard

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

    // Theme colors
    private val darkBg       = 0xFF0D1117.toInt()
    private val darkSurface  = 0xFF161B22.toInt()
    private val darkText     = 0xFFC9D1D9.toInt()
    private val darkSubText  = 0xFF8B949E.toInt()
    private val darkBorder   = 0xFF30363D.toInt()
    private val lightBg      = 0xFFF0F4F8.toInt()
    private val lightSurface = 0xFFFFFFFF.toInt()
    private val lightText    = 0xFF0D1117.toInt()
    private val lightSubText = 0xFF57606A.toInt()
    private val accentBlue   = 0xFF1F6FEB.toInt()
    private val panicRed     = 0xFFCC0000.toInt()
    private val panicRedHint = 0xFF8B0000.toInt()
    private val white        = 0xFFFFFFFF.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("nexguard_prefs", MODE_PRIVATE)
        isDark = prefs.getBoolean("dark_theme", true)
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), 1001)

        registerDevice(deviceId)
        startForegroundService(Intent(this, ForegroundProtectionService::class.java))

        buildUI()
    }

    private fun buildUI() {
        val bg      = if (isDark) darkBg      else lightBg
        val surface = if (isDark) darkSurface else lightSurface
        val text    = if (isDark) darkText    else lightText
        val subText = if (isDark) darkSubText else lightSubText

        // Root scroll
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 60, 48, 60)
            setBackgroundColor(bg)
        }

        // ── Header Row ──
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

        // ── Status Card ──
        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)
            background = roundedBg(surface, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 28) }
        }

        val statusDot = TextView(this).apply {
            this.text = "● PROTECTION ACTIVE"
            textSize = 13f
            setTextColor(0xFF238636.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val deviceLabel = TextView(this).apply {
            this.text = "Device: ${deviceId.take(16)}..."
            textSize = 11f
            setTextColor(subText)
            setPadding(0, 6, 0, 0)
        }

        statusCard.addView(statusDot)
        statusCard.addView(deviceLabel)
        root.addView(statusCard)

        // ── PANIC BUTTON ──
        val panicBtn = Button(this).apply {
            this.text = "🚨 TRIGGER PANIC"
            textSize = 20f
            setTextColor(white)
            setBackgroundColor(panicRed)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 180
            ).apply { setMargins(0, 0, 0, 12) }
            background = roundedBg(panicRed, 16)
        }

        val panicHint = TextView(this).apply {
            this.text = "or hold Volume Down 3× to trigger silently"
            textSize = 11f
            setTextColor(subText)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 36) }
        }

        val panicStatus = TextView(this).apply {
            this.text = ""
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(0xFF238636.toInt())
        }

        panicBtn.setOnClickListener {
            try {
                PanicService(this).triggerPanic(deviceId)
                panicStatus.text = "✓ Panic alert sent"
                panicStatus.setTextColor(0xFF238636.toInt())
            } catch (e: Exception) {
                panicStatus.text = "Error: ${e.message}"
                panicStatus.setTextColor(0xFFF85149.toInt())
                Log.e("NexGuard", "Panic error: ${e.message}")
            }
        }

        root.addView(panicBtn)
        root.addView(panicHint)
        root.addView(panicStatus)

        // ── Section: Emergency Contacts ──
        root.addView(sectionHeader("Emergency Contacts", text))

        val contact1Name  = styledEdit("Contact 1 — Full Name", surface, text, subText)
        val contact1Phone = styledEdit("Contact 1 — Phone (+234...)", surface, text, subText)
        val contact2Name  = styledEdit("Contact 2 — Full Name", surface, text, subText)
        val contact2Phone = styledEdit("Contact 2 — Phone (+234...)", surface, text, subText)
        val contact3Name  = styledEdit("Contact 3 — Full Name", surface, text, subText)
        val contact3Phone = styledEdit("Contact 3 — Phone (+234...)", surface, text, subText)

        listOf(contact1Name, contact1Phone, contact2Name,
               contact2Phone, contact3Name, contact3Phone)
            .forEach { root.addView(it) }

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
        }

        saveBtn.setOnClickListener {
            val contacts = mutableListOf<ContactItem>()
            val n1 = contact1Name.text.toString().trim()
            val p1 = contact1Phone.text.toString().trim()
            val n2 = contact2Name.text.toString().trim()
            val p2 = contact2Phone.text.toString().trim()
            val n3 = contact3Name.text.toString().trim()
            val p3 = contact3Phone.text.toString().trim()

            if (n1.isNotEmpty() && p1.isNotEmpty()) contacts.add(ContactItem(n1, p1))
            if (n2.isNotEmpty() && p2.isNotEmpty()) contacts.add(ContactItem(n2, p2))
            if (n3.isNotEmpty() && p3.isNotEmpty()) contacts.add(ContactItem(n3, p3))

            if (contacts.isEmpty()) {
                saveStatus.text = "Add at least one contact"
                saveStatus.setTextColor(0xFFF85149.toInt())
                return@setOnClickListener
            }
            saveContacts(deviceId, contacts, saveStatus)
        }

        root.addView(saveBtn)
        root.addView(saveStatus)

        // ── Footer ──
        val footer = TextView(this).apply {
            this.text = "NexGuard v1.0 — CyberNexus Technologies"
            textSize = 11f
            setTextColor(subText)
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        }
        root.addView(footer)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun sectionHeader(label: String, textColor: Int): TextView {
        return TextView(this).apply {
            text = label
            textSize = 15f
            setTextColor(textColor)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 12) }
        }
    }

    private fun styledEdit(hint: String, bg: Int, textColor: Int, hintColor: Int): EditText {
        return EditText(this).apply {
            this.hint = hint
            setHintTextColor(hintColor)
            setTextColor(textColor)
            setBackgroundColor(bg)
            setPadding(24, 20, 24, 20)
            textSize = 14f
            background = roundedBg(bg, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 12) }
        }
    }

    private fun roundedBg(color: Int, radiusDp: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = radiusDp * resources.displayMetrics.density
        }
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
                        status.setTextColor(0xFF238636.toInt())
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    runOnUiThread {
                        status.text = "Failed to save contacts"
                        status.setTextColor(0xFFF85149.toInt())
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("NexGuard", "Contacts crash: ${e.message}")
        }
    }
}
