package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import javax.inject.Inject

class RefreshBookingsUseCase @Inject constructor(private val repository: BookingRepository) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.refreshBookings()
    }
}