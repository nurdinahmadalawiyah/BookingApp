package com.dinzio.bookingapp.features.booking.presentation.viewModel

import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity

data class BookingUiState(
    val isLoading: Boolean = false,
    val bookings: List<BookingEntity> = emptyList(),
    val bookingDetail: BookingEntity? = null,
    val searchQuery: String = "",
    val error: String? = null
)