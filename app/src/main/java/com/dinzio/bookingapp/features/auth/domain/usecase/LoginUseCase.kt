package com.dinzio.bookingapp.features.auth.domain.usecase

import com.dinzio.bookingapp.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(username: String, password: String) = repository.login(username, password)
}