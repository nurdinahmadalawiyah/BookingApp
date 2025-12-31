package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteBookingUseCaseTest {

    private val repository: BookingRepository = mockk()
    private val useCase = DeleteBookingUseCase(repository)

    @Test
    fun `invoke should call deleteBooking on repository and return result`() = runTest {
        // Arrange
        val token = "valid_token"
        val bookingId = 123
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.deleteBooking(token, bookingId) } returns expectedResult

        // Act
        val result = useCase(token, bookingId)

        // Assert
        assertEquals(expectedResult, result)
        coVerify(exactly = 1) { repository.deleteBooking(token, bookingId) }
    }

    @Test
    fun `invoke should return error when repository delete fails`() = runTest {
        // Arrange
        val token = "valid_token"
        val bookingId = 456
        val errorMessage = "Not Authorized"
        val expectedResult = Resource.Error<Unit>(errorMessage)

        coEvery { repository.deleteBooking(token, bookingId) } returns expectedResult

        // Act
        val result = useCase(token, bookingId)

        // Assert
        assert(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)

        coVerify(exactly = 1) { repository.deleteBooking(token, bookingId) }
    }
}