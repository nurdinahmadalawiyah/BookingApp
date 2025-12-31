package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateBookingUseCaseTest {

    private val repository: BookingRepository = mockk()
    private val useCase = UpdateBookingUseCase(repository)

    @Test
    fun `invoke should call updateBooking on repository`() = runTest {
        // Arrange
        val token = "mock_token"
        val entity = BookingEntity(
            bookingid = 123,
            firstname = "John",
            lastname = "Doe",
            totalprice = 100,
            depositpaid = true,
            checkin = "",
            checkout = "",
            additionalneeds = ""
        )
        val expectedResource = Resource.Success(entity)

        coEvery { repository.updateBooking(token, entity) } returns expectedResource

        // Act
        val result = useCase(token, entity)

        // Assert
        assert(result is Resource.Success)
        assertEquals(entity, (result as Resource.Success).data)
    }
}