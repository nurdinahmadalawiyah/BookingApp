package com.dinzio.bookingapp.features.auth.data.repository

import app.cash.turbine.test
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.core.data.local.TokenManager
import com.dinzio.bookingapp.features.auth.data.model.AuthResponse
import com.dinzio.bookingapp.features.auth.data.source.AuthApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRepositoryImplTest {

    private val apiService: AuthApiService = mockk()
    private val tokenManager: TokenManager = mockk(relaxed = true)
    private val repository = AuthRepositoryImpl(apiService, tokenManager)

    @Test
    fun `login success should save token and emit success`() = runTest {
        val token = "valid_token"
        coEvery { apiService.login(any()) } returns AuthResponse(token = token)

        repository.login("user", "pass").test {
            assertEquals(true, awaitItem() is Resource.Loading)
            val successItem = awaitItem()
            assertEquals(token, (successItem as Resource.Success).data)
            coVerify { tokenManager.saveToken(token) }
            awaitComplete()
        }
    }

    @Test
    fun `login failure with reason should emit error message`() = runTest {
        val reason = "Wrong Password"
        coEvery { apiService.login(any()) } returns AuthResponse(reason = reason)

        repository.login("user", "pass").test {
            awaitItem() // Loading
            val errorItem = awaitItem()
            assertEquals(reason, (errorItem as Resource.Error).message)
            awaitComplete()
        }
    }
}