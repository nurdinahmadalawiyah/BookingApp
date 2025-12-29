package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import javax.inject.Inject

class DeleteBookingUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(token: String, id: Int): Resource<Unit> {
        return repository.deleteBooking(token, id)
    }
}