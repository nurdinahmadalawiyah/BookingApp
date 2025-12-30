package com.dinzio.bookingapp.features.booking.presentation.viewModel

import android.app.Application
import androidx.work.WorkManager
import app.cash.turbine.test
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.core.data.local.TokenManager
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.usecase.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    // Mocks
    private val getBookingsUseCase: GetBookingsUseCase = mockk()
    private val refreshBookingsUseCase: RefreshBookingsUseCase = mockk()
    private val getBookingDetailUseCase: GetBookingDetailUseCase = mockk()
    private val createBookingUseCase: CreateBookingUseCase = mockk()
    private val updateBookingUseCase: UpdateBookingUseCase = mockk()
    private val deleteBookingUseCase: DeleteBookingUseCase = mockk()
    private val tokenManager: TokenManager = mockk()
    private val workManager: WorkManager = mockk(relaxed = true)
    private lateinit var viewModel: BookingViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getBookingsUseCase() } returns flowOf(emptyList())
        coEvery { tokenManager.getToken() } returns flowOf("fake_token")
        coEvery { refreshBookingsUseCase(any()) } returns Resource.Success(Unit)

        viewModel = BookingViewModel(
            getBookingsUseCase,
            refreshBookingsUseCase,
            getBookingDetailUseCase,
            createBookingUseCase,
            updateBookingUseCase,
            deleteBookingUseCase,
            tokenManager,
            workManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refresh success should update loading state to false and call usecase`() = runTest {
        // Arrange
        coEvery { refreshBookingsUseCase(any()) } coAnswers {
            delay(100)
            Resource.Success(Unit)
        }

        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(false, initialState.isLoading)

            // Act
            viewModel.refresh()

            // Assert
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            val successState = awaitItem()
            assertEquals(false, successState.isLoading)

            coVerify { refreshBookingsUseCase("") }
        }
    }

    @Test
    fun `refresh failure should update state with error message`() = runTest {
        // Arrange
        val errorMsg = "Network Error"
        coEvery { refreshBookingsUseCase(any()) } coAnswers {
            delay(100)
            Resource.Error(errorMsg)
        }

        viewModel.state.test {
            awaitItem()

            // Act
            viewModel.refresh()

            // Assert
            val loadingItem = awaitItem()
            assertEquals(true, loadingItem.isLoading)

            val errorItem = awaitItem()
            assertEquals(false, errorItem.isLoading)
            assertEquals(errorMsg, errorItem.error)
        }
    }

    @Test
    fun `getBookingDetail success should update bookingDetail in state`() = runTest {
        // Arrange
        val bookingId = 123
        val mockBooking = BookingEntity(bookingid = bookingId, firstname = "John", lastname = "Doe", totalprice = 100, depositpaid = true, checkin = "", checkout = "", additionalneeds = "")

        // Gunakan delay kecil agar Turbine sempat menangkap state loading
        coEvery { getBookingDetailUseCase(bookingId) } coAnswers {
            delay(10)
            Resource.Success(mockBooking)
        }

        viewModel.state.test {
            // 1. Cek State Awal
            assertEquals(null, awaitItem().bookingDetail)

            // 2. Trigger Action
            viewModel.getBookingDetail(bookingId)

            // 3. Tangkap State Loading (true)
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            // 4. Tangkap State Final (false + data)
            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertEquals(mockBooking, finalState.bookingDetail)
        }
    }

    @Test
    fun `getBookingDetail failure should update error in state`() = runTest {
        // Arrange
        val bookingId = 123
        val errorMessage = "Booking not found"
        coEvery { getBookingDetailUseCase(bookingId) } coAnswers {
            delay(10)
            Resource.Error(errorMessage)
        }

        viewModel.state.test {
            awaitItem() // Skip state awal

            // Act
            viewModel.getBookingDetail(bookingId)

            // Assert Loading
            assertEquals(true, awaitItem().isLoading)

            // Assert Error Result
            val errorState = awaitItem()
            assertEquals(false, errorState.isLoading)
            assertEquals(errorMessage, errorState.error)
        }
    }
}