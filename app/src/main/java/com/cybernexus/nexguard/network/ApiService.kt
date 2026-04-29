package com.cybernexus.nexguard.network

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("panic")
    suspend fun sendPanic(@Body data: Map<String, Any>)
}
