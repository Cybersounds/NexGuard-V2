package com.cybernexus.nexguard.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class PanicRequest(
    val deviceId: String,
    val message: String
)

interface ApiService {

    @POST("panic")
    fun sendPanic(@Body request: PanicRequest): Call<Void>
}
