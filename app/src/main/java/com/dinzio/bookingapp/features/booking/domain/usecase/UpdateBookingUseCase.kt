package com.dinzio.bookingapp.features.booking.domain.usecase

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import javax.inject.Inject

class UpdateBookingUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(token: String, entity: BookingEntity): Resource<BookingEntity> {
        return repository.updateBooking(token, entity)
    }
}