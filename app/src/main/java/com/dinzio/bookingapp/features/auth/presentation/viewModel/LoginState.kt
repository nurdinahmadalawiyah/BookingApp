package com.dinzio.bookingapp.features.auth.presentation.viewModel

data class LoginState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val error: String? = null,
    val isLoginSuccess: Boolean = false
)