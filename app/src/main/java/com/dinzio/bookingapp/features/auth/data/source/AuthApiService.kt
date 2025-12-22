package com.dinzio.bookingapp.features.auth.data.source

import com.dinzio.bookingapp.features.auth.data.model.AuthRequest
import com.dinzio.bookingapp.features.auth.data.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}