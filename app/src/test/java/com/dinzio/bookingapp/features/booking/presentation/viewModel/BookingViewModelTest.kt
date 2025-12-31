package com.dinzio.bookingapp.features.booking.presentation.viewModel

import androidx.work.WorkManager
import app.cash.turbine.test
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.core.data.local.TokenManager
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.usecase.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
        val mockBooking = BookingEntity(
            bookingid = bookingId,
            firstname = "John",
            lastname = "Doe",
            totalprice = 100,
            depositpaid = true,
            checkin = "",
            checkout = "",
            additionalneeds = ""
        )

        coEvery { getBookingDetailUseCase(bookingId) } coAnswers {
            delay(10)
            Resource.Success(mockBooking)
        }

        viewModel.state.test {
            assertEquals(null, awaitItem().bookingDetail)
            viewModel.getBookingDetail(bookingId)

            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

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
            awaitItem()

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

    @Test
    fun `createBooking success should emit BookingCreated event`() = runTest {
        // Arrange
        val mockBooking = BookingEntity(
            bookingid = 0,
            firstname = "Dinzio",
            lastname = "Dev",
            totalprice = 200,
            depositpaid = true,
            checkin = "2023-10-01",
            checkout = "2023-10-05",
            additionalneeds = "Breakfast"
        )
        val successResult = mockBooking.copy(bookingid = 999)

        coEvery { createBookingUseCase(any()) } returns Resource.Success(successResult)

        viewModel.event.test {
            // Act
            viewModel.createBooking(mockBooking)

            // Assert Event
            val event = awaitItem()
            assert(event is BookingUiEvent.BookingCreated)
            assertEquals(successResult, (event as BookingUiEvent.BookingCreated).data)
        }
    }

    @Test
    fun `createBooking failure should emit ShowError event`() = runTest {
        // Arrange
        val mockBooking = BookingEntity(
            bookingid = 0,
            firstname = "Fail",
            lastname = "Test",
            totalprice = 0,
            depositpaid = false,
            checkin = "",
            checkout = "",
            additionalneeds = ""
        )
        val errorMessage = "Failed to create booking"

        coEvery { createBookingUseCase(any()) } returns Resource.Error(errorMessage)

        viewModel.event.test {
            // Act
            viewModel.createBooking(mockBooking)

            // Assert Event
            val event = awaitItem()
            assert(event is BookingUiEvent.ShowError)
            assertEquals(errorMessage, (event as BookingUiEvent.ShowError).message)
        }
    }

    @Test
    fun `updateBooking success should emit BookingUpdated event`() = runTest {
        // Arrange
        val token = "fake_token"
        val mockBooking = BookingEntity(
            bookingid = 123,
            firstname = "John",
            lastname = "Update",
            totalprice = 150,
            depositpaid = true,
            checkin = "2023-10-01",
            checkout = "2023-10-05",
            additionalneeds = "Late Checkout"
        )

        every { tokenManager.getToken() } returns flowOf(token)

        coEvery { updateBookingUseCase(any(), any()) } returns Resource.Success(mockBooking)

        // Act & Assert
        viewModel.event.test {
            viewModel.updateBooking(mockBooking)

            val event = awaitItem()

            assert(event is BookingUiEvent.BookingUpdated)
            assertEquals(mockBooking.bookingid, (event as BookingUiEvent.BookingUpdated).data.bookingid)
        }

        // Verify
        coVerify { updateBookingUseCase(any(), any()) }
    }

    @Test
    fun `updateBooking failure should emit ShowError event`() = runTest {
        // Arrange
        val token = "valid_token"
        val mockBooking = BookingEntity(
            bookingid = 123,
            firstname = "X",
            lastname = "Y",
            totalprice = 0,
            depositpaid = false,
            checkin = "",
            checkout = "",
            additionalneeds = ""
        )
        val errorMsg = "Update Failed"

        every { tokenManager.getToken() } returns flowOf(token)
        coEvery { updateBookingUseCase(any(), any()) } returns Resource.Error(errorMsg)

        viewModel.event.test {
            // Act
            viewModel.updateBooking(mockBooking)

            // Assert
            val event = awaitItem()
            assert(event is BookingUiEvent.ShowError)
            assertEquals(errorMsg, (event as BookingUiEvent.ShowError).message)
        }
    }

    @Test
    fun `deleteBooking success should emit BookingDeleted event`() = runTest {
        // Arrange
        val token = "fake_token"
        val bookingId = 123

        every { tokenManager.getToken() } returns flowOf(token)

        coEvery { deleteBookingUseCase(any(), eq(bookingId)) } returns Resource.Success(Unit)

        viewModel.event.test {
            // Act
            viewModel.deleteBooking(bookingId)

            // Assert
            val event = awaitItem()
            assert(event is BookingUiEvent.BookingDeleted)
        }

        coVerify { deleteBookingUseCase(any(), eq(bookingId)) }
    }

    @Test
    fun `deleteBooking failure should emit ShowError event`() = runTest {
        // Arrange
        val token = "fake_token"
        val bookingId = 123
        val errorMessage = "Delete failed"

        every { tokenManager.getToken() } returns flowOf(token)

        coEvery { deleteBookingUseCase(any(), eq(bookingId)) } returns Resource.Error(errorMessage)

        viewModel.event.test {
            // Act
            viewModel.deleteBooking(bookingId)

            // Assert
            val event = awaitItem()
            assert(event is BookingUiEvent.ShowError)
            assertEquals(errorMessage, (event as BookingUiEvent.ShowError).message)
        }
    }
}