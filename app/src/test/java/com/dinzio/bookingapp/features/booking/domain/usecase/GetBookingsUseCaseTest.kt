package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class GetBookingsUseCaseTest {
    private val repository: BookingRepository = mockk()
    private val useCase = GetBookingsUseCase(repository)

    @Test
    fun `invoke should call getBookingsFromDb on repository`() {
        // Arrange
        val mockFlow = flowOf(emptyList<BookingEntity>())
        every { repository.getBookingsFromDb() } returns mockFlow

        // Act
        val result = useCase()

        // Assert
        assertEquals(mockFlow, result)
        verify { repository.getBookingsFromDb() }
    }
}