package com.cybernexus.nexguard.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class PanicRequest(
    val deviceId: String,
    val message: String,
    val latitude: Double?,
    val longitude: Double?
)

data class RegisterRequest(
    val deviceId: String
)

interface ApiService {
    @POST("panic.php")
    fun sendPanic(@Body request: PanicRequest): Call<Void>

    @POST("register.php")
    fun registerDevice(@Body request: RegisterRequest): Call<Void>

    @POST("contacts.php")
    fun saveContacts(@Body request: ContactsRequest): Call<Void>
}
