package com.dinzio.bookingapp.features.auth.presentation.viewModel

import app.cash.turbine.test
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.core.data.local.TokenManager
import com.dinzio.bookingapp.features.auth.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val loginUseCase: LoginUseCase = mockk()
    private val tokenManager: TokenManager = mockk(relaxed = true)
    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginUseCase, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login flow should update state correctly`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Success("token")
        )

        viewModel.state.test {
            assertEquals(LoginState(), awaitItem())

            viewModel.login("admin", "password123")

            advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertEquals(false, finalState.isLoading)
            assertEquals(true, finalState.isLoginSuccess)
            assertEquals(null, finalState.error)
        }
    }

    @Test
    fun `logout should clear token and reset state`() = runTest {
        viewModel.logout()

        coVerify { tokenManager.clearToken() }
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(false, state.isLoginSuccess)
        }
    }
}