package com.cybernexus.nexguard

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cybernexus.nexguard.network.*
import com.cybernexus.nexguard.services.PanicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var deviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1001
        )

        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        registerDevice(deviceId)

        // --- Layout ---
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(60, 80, 60, 80)
        }

        // Logo
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.nexguard_logo)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                200
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, 40)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }  
        layout.addView(logo)


        // Contact fields
        val contact1Name  = editText("Contact 1 Name")
        val contact1Phone = editText("Contact 1 Phone (+234...)")
        val contact2Name  = editText("Contact 2 Name")
        val contact2Phone = editText("Contact 2 Phone (+234...)")
        val contact3Name  = editText("Contact 3 Name")
        val contact3Phone = editText("Contact 3 Phone (+234...)")

        layout.addView(sectionLabel("Emergency Contacts"))
        layout.addView(contact1Name)
        layout.addView(contact1Phone)
        layout.addView(contact2Name)
        layout.addView(contact2Phone)
        layout.addView(contact3Name)
        layout.addView(contact3Phone)

        // Save contacts button
        val saveBtn = Button(this).apply {
            text = "SAVE CONTACTS"
            setPadding(0, 20, 0, 20)
        }

        val statusText = TextView(this).apply {
            text = ""
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 10)
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
                statusText.text = "Add at least one contact"
                return@setOnClickListener
            }

            saveContacts(deviceId, contacts, statusText)
        }

        layout.addView(saveBtn)
        layout.addView(statusText)

        // Panic button
        layout.addView(sectionLabel(""))
        val panicBtn = Button(this).apply {
            text = "TRIGGER PANIC"
            setBackgroundColor(0xFFCC0000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            setPadding(0, 40, 0, 40)
        }

        panicBtn.setOnClickListener {
            try {
                PanicService(this).triggerPanic(deviceId)
                statusText.text = "Panic triggered!"
            } catch (e: Exception) {
                Log.e("NexGuard", "Button crash: ${e.message}")
            }
        }

        layout.addView(panicBtn)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun editText(hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
        }
    }

    private fun sectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setPadding(0, 24, 0, 4)
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
            val request = ContactsRequest(deviceId, contacts)
            api.saveContacts(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    runOnUiThread { status.text = "${contacts.size} contact(s) saved ✓" }
                    Log.d("NexGuard", "Contacts saved: ${response.code()}")
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    runOnUiThread { status.text = "Failed to save contacts" }
                    Log.e("NexGuard", "Contacts failed: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("NexGuard", "Contacts crash: ${e.message}")
        }
    }
}
