package com.dinzio.bookingapp.features.booking.domain.repository

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun getBookingsFromDb(): Flow<List<BookingEntity>>

    suspend fun refreshBookings(): Resource<Unit>

    suspend fun loadMoreBookings(startIndex: Int, count: Int): Resource<Unit>
}