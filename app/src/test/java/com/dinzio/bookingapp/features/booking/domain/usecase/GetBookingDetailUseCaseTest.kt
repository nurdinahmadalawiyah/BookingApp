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

class GetBookingDetailUseCaseTest {

    private val repository: BookingRepository = mockk()
    private val useCase = GetBookingDetailUseCase(repository)

    @Test
    fun `invoke should call getBookingDetailById on repository and return result`() = runTest {
        // Arrange
        val bookingId = 123
        val mockBooking = BookingEntity(
            bookingid = bookingId,
            firstname = "John",
            lastname = "Doe",
            totalprice = 100,
            depositpaid = true,
            checkin = "2023-01-01",
            checkout = "2023-01-02",
            additionalneeds = "WiFi"
        )
        coEvery { repository.getBookingDetailById(bookingId) } returns Resource.Success(mockBooking)

        // Act
        val result = useCase(bookingId)

        // Assert
        assert(result is Resource.Success)
        assertEquals(mockBooking, (result as Resource.Success).data)
        coVerify { repository.getBookingDetailById(bookingId) }
    }
}