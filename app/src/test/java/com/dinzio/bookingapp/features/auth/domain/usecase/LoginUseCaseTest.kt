package com.dinzio.bookingapp.features.auth.domain.usecase

import com.dinzio.bookingapp.features.auth.domain.repository.AuthRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class LoginUseCaseTest {

    private val repository: AuthRepository = mockk(relaxed = true)
    private val loginUseCase = LoginUseCase(repository)

    @Test
    fun `invoke should call repository login`() {
        val username = "admin"
        val password = "password123"

        loginUseCase(username, password)

        verify { repository.login(username, password) }
    }
}