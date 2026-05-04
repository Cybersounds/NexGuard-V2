package com.cybernexus.nexguard.services

import android.util.Log
import com.cybernexus.nexguard.network.ApiClient
import com.cybernexus.nexguard.network.ApiService
import com.cybernexus.nexguard.network.PanicRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PanicService {

    fun triggerPanic(deviceId: String) {
        try {
            val api = ApiClient.retrofit.create(ApiService::class.java)

            val request = PanicRequest(
                deviceId = deviceId,
                message = "Emergency triggered"
            )

            api.sendPanic(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("NexGuard", "Response: ${response.code()}")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("NexGuard", "Error: ${t.message}")
                }
            })

        } catch (e: Exception) {
            Log.e("NexGuard", "Crash prevented: ${e.message}")
        }
    }
}
