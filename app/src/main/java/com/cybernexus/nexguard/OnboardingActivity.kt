package com.cybernexus.nexguard

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cybernexus.nexguard.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.cybernexus.nexguard.services.ForegroundProtectionService

class OnboardingActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var deviceId: String
    private var currentStep = 0

    // Colors
    private val bg          = 0xFFFFFFFF.toInt()
    private val accent      = 0xFF1F6FEB.toInt()
    private val panicRed    = 0xFFCC0000.toInt()
    private val textDark    = 0xFF0D1117.toInt()
    private val textGrey    = 0xFF57606A.toInt()
    private val green       = 0xFF238636.toInt()
    private val white       = 0xFFFFFFFF.toInt()
    private val lightGrey   = 0xFFF0F4F8.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("nexguard_prefs", MODE_PRIVATE)
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        showStep(0)
    }

    private fun showStep(step: Int) {
        currentStep = step
        when (step) {
            0 -> showWelcome()
            1 -> showContactSetup()
            2 -> showTestPanic()
            3 -> showComplete()
        }
    }

    // ── STEP 0: Welcome ──
    private fun showWelcome() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(60, 100, 60, 60)
            setBackgroundColor(bg)
        }

        root.addView(TextView(this).apply {
            text = "🛡️"
            textSize = 72f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
        })

        root.addView(TextView(this).apply {
            text = "Welcome to NexGuard"
            textSize = 26f
            setTextColor(textDark)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        })

        root.addView(TextView(this).apply {
            text = "Your personal emergency safety system.\n\nOne tap sends your GPS location to up to 3 emergency contacts instantly via SMS."
            textSize = 16f
            setTextColor(textGrey)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 48) }
        })

        // Feature bullets
        listOf(
            "📍 Real-time GPS location sharing",
            "📱 Works even with screen off",
            "🔇 Silent trigger via volume button",
            "🕵️ Optional stealth mode"
        ).forEach { feature ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24, 14, 24, 14)
                background = roundedBg(lightGrey, 10)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 10) }
            }
            row.addView(TextView(this).apply {
                text = feature
                textSize = 14f
                setTextColor(textDark)
            })
            root.addView(row)
        }

        root.addView(stepIndicator(0))

        root.addView(Button(this).apply {
            text = "GET STARTED →"
            textSize = 16f
            setTextColor(white)
            background = roundedBg(accent, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 40, 0, 0) }
            setOnClickListener { showStep(1) }
        })

        scroll.addView(root)
        setContentView(scroll)
    }

    // ── STEP 1: Add Contacts ──
    private fun showContactSetup() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 80, 60, 60)
            setBackgroundColor(bg)
        }

        root.addView(TextView(this).apply {
            text = "👥"
            textSize = 52f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        })


        root.addView(TextView(this).apply {
	    text = "⚠️ Emergency contacts must have DND disabled. Send START to 2442 to disable DND."
	    textSize = 12f
	    setTextColor(0xFFF0A500.toInt())
	    gravity = Gravity.CENTER
	    layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        })

        root.addView(TextView(this).apply {
            text = "Add Emergency Contacts"
            textSize = 22f
            setTextColor(textDark)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 10) }
        })

        root.addView(TextView(this).apply {
            text = "These people will receive your location via SMS when you trigger a panic alert."
            textSize = 14f
            setTextColor(textGrey)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 32) }
        })

        val c1n = styledEdit("Contact 1 — Full Name")
        val c1p = styledEdit("Contact 1 — Phone (+2348...)")
        val c2n = styledEdit("Contact 2 — Full Name (optional)")
        val c2p = styledEdit("Contact 2 — Phone (optional)")
        val c3n = styledEdit("Contact 3 — Full Name (optional)")
        val c3p = styledEdit("Contact 3 — Phone (optional)")

        listOf(c1n, c1p, c2n, c2p, c3n, c3p).forEach { root.addView(it) }

        val statusText = TextView(this).apply {
            text = ""
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }

        root.addView(stepIndicator(1))

        root.addView(Button(this).apply {
            text = "SAVE & CONTINUE →"
            textSize = 15f
            setTextColor(white)
            background = roundedBg(accent, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 24, 0, 8) }
            setOnClickListener {
                val contacts = mutableListOf<ContactItem>()
                if (c1n.text.isNotEmpty() && c1p.text.isNotEmpty())
                    contacts.add(ContactItem(c1n.text.toString().trim(), c1p.text.toString().trim()))
                if (c2n.text.isNotEmpty() && c2p.text.isNotEmpty())
                    contacts.add(ContactItem(c2n.text.toString().trim(), c2p.text.toString().trim()))
                if (c3n.text.isNotEmpty() && c3p.text.isNotEmpty())
                    contacts.add(ContactItem(c3n.text.toString().trim(), c3p.text.toString().trim()))

                if (contacts.isEmpty()) {
                    statusText.text = "⚠ Add at least one contact to continue"
                    statusText.setTextColor(0xFFF85149.toInt())
                    return@setOnClickListener
                }

                val api = ApiClient.retrofit.create(ApiService::class.java)
                api.saveContacts(ContactsRequest(deviceId, contacts))
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            runOnUiThread { showStep(2) }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            runOnUiThread {
                                statusText.text = "Failed to save. Check connection."
                                statusText.setTextColor(0xFFF85149.toInt())
                            }
                        }
                    })
            }
        })

        root.addView(TextView(this).apply {
            text = "SKIP FOR NOW"
            textSize = 13f
            setTextColor(textGrey)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
            setOnClickListener { showStep(2) }
        })

        root.addView(statusText)
        scroll.addView(root)
        setContentView(scroll)
    }

    // ── STEP 2: Test Panic ──
    private fun showTestPanic() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(60, 100, 60, 60)
            setBackgroundColor(bg)
        }

        root.addView(TextView(this).apply {
            text = "🚨"
            textSize = 72f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
        })

        root.addView(TextView(this).apply {
            text = "Test Your Panic Button"
            textSize = 22f
            setTextColor(textDark)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 10) }
        })

        root.addView(TextView(this).apply {
            text = "Tap the button below to send a test alert to your contacts. They will receive an SMS with your location."
            textSize = 14f
            setTextColor(textGrey)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 40) }
        })

        val testStatus = TextView(this).apply {
            text = ""
            textSize = 14f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
        }

        root.addView(Button(this).apply {
            text = "🚨  SEND TEST ALERT"
            textSize = 18f
            setTextColor(white)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = roundedBg(panicRed, 14)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160
            ).apply { setMargins(0, 0, 0, 16) }
            setOnClickListener {
                com.cybernexus.nexguard.services.PanicService(this@OnboardingActivity)
                    .triggerPanic(deviceId)
                testStatus.text = "✓ Test alert sent! Check your contacts' phones."
                testStatus.setTextColor(green)
            }
        })

        root.addView(testStatus)

        root.addView(TextView(this).apply {
            text = "💡 You can also hold Volume Down 3× to trigger silently"
            textSize = 12f
            setTextColor(textGrey)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 40) }
        })

        root.addView(stepIndicator(2))

        root.addView(Button(this).apply {
            text = "CONTINUE →"
            textSize = 15f
            setTextColor(white)
            background = roundedBg(accent, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 24, 0, 0) }
            setOnClickListener { showStep(3) }
        })

        scroll.addView(root)
        setContentView(scroll)
    }

    // ── STEP 3: Complete ──
    private fun showComplete() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(60, 120, 60, 60)
            setBackgroundColor(bg)
        }

        root.addView(TextView(this).apply {
            text = "✅"
            textSize = 72f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
        })

        root.addView(TextView(this).apply {
            text = "You're Protected"
            textSize = 26f
            setTextColor(textDark)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        })

        root.addView(TextView(this).apply {
            text = "NexGuard is active and running in the background. Your emergency contacts will be notified immediately if you ever need help."
            textSize = 15f
            setTextColor(textGrey)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 48) }
        })

        listOf(
            "✅ Emergency contacts saved",
            "✅ GPS tracking enabled",
            "✅ Background protection active",
            "✅ Volume button trigger ready"
        ).forEach { item ->
            root.addView(TextView(this).apply {
                text = item
                textSize = 15f
                setTextColor(textDark)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 12) }
            })
        }

        root.addView(Button(this).apply {
            text = "OPEN NEXGUARD →"
            textSize = 16f
            setTextColor(white)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = roundedBg(green, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 48, 0, 0) }
            setOnClickListener {
                prefs.edit().putBoolean("onboarding_done", true).apply()
                // Start protection service now that permissions are granted
                startForegroundService(
                    Intent(this@OnboardingActivity, ForegroundProtectionService::class.java)
                )
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }
        })

        scroll.addView(root)
        setContentView(scroll)
    }

    // ── Helpers ──
    private fun stepIndicator(current: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 32, 0, 0) }
            for (i in 0..3) {
                addView(TextView(this@OnboardingActivity).apply {
                    text = "●"
                    textSize = 14f
                    setTextColor(if (i == current) accent else 0xFFD0D7DE.toInt())
                    setPadding(8, 0, 8, 0)
                })
            }
        }
    }

    private fun styledEdit(hint: String) = EditText(this).apply {
        this.hint = hint
        setHintTextColor(0xFF8B949E.toInt())
        setTextColor(textDark)
        setPadding(24, 20, 24, 20)
        textSize = 14f
        background = roundedBg(lightGrey, 10)
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
}
