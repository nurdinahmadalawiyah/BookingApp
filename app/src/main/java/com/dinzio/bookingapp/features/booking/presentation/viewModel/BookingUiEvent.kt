package com.dinzio.bookingapp.features.booking.presentation.viewModel

import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity

sealed interface BookingUiEvent {
    data class BookingCreated(val data: BookingEntity) : BookingUiEvent
    data class BookingUpdated(val data: BookingEntity) : BookingUiEvent
    data object BookingDeleted : BookingUiEvent
    data class ShowError(val message: String) : BookingUiEvent
}