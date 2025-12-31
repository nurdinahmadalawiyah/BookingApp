package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateBookingUseCaseTest {

    private val repository: BookingRepository = mockk()
    private val useCase = CreateBookingUseCase(repository)

    @Test
    fun `invoke should call createBooking on repository and return success`() = runTest {
        // Arrange
        val bookingRequest = BookingEntity(
            bookingid = 0,
            firstname = "Dinzio",
            lastname = "Dev",
            totalprice = 150,
            depositpaid = true,
            checkin = "2023-10-10",
            checkout = "2023-10-12",
            additionalneeds = "Late check-in"
        )
        val expectedResponse = bookingRequest.copy(bookingid = 1)

        coEvery { repository.createBooking(bookingRequest) } returns Resource.Success(expectedResponse)

        // Act
        val result = useCase(bookingRequest)

        // Assert
        assert(result is Resource.Success)
        assertEquals(expectedResponse, (result as Resource.Success).data)

        coVerify { repository.createBooking(bookingRequest) }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Arrange
        val bookingRequest = BookingEntity(
            bookingid = 0,
            firstname = "Fail",
            lastname = "Test",
            totalprice = 0,
            depositpaid = false,
            checkin = "",
            checkout = "",
            additionalneeds = ""
        )
        val errorMessage = "Network Error"

        coEvery { repository.createBooking(bookingRequest) } returns Resource.Error(errorMessage)

        // Act
        val result = useCase(bookingRequest)

        // Assert
        assert(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)

        coVerify { repository.createBooking(bookingRequest) }
    }
}