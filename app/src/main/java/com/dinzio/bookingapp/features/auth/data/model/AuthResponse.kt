package com.dinzio.bookingapp.features.auth.data.model

data class AuthResponse(
    val token: String? = null,
    val reason: String? = null,
)