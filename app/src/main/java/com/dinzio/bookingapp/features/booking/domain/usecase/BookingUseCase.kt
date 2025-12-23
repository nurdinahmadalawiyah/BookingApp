package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookingsUseCase @Inject constructor(private val repository: BookingRepository) {
    operator fun invoke(): Flow<List<BookingEntity>> {
        return repository.getBookingsFromDb()
    }
}