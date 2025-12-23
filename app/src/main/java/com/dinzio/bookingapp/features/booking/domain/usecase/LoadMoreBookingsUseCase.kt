package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import javax.inject.Inject

class LoadMoreBookingsUseCase @Inject constructor(private val repository: BookingRepository) {
    suspend operator fun invoke(startIndex: Int, count: Int) = repository.loadMoreBookings(startIndex, count)
}