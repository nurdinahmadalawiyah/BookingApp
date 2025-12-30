package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshBookingsUseCaseTest {
    private val repository: BookingRepository = mockk()
    private val useCase = RefreshBookingsUseCase(repository)

    @Test
    fun `invoke should call refreshBookings on repository and return result`() = runTest {
        // Arrange
        val query = "Nurdin"
        coEvery { repository.refreshBookings(query) } returns Resource.Success(Unit)

        // Act
        val result = useCase(query)

        // Assert
        assert(result is Resource.Success)
        coVerify { repository.refreshBookings(query) }
    }
}