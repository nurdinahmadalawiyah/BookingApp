package com.dinzio.bookingapp.features.booking.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.domain.usecase.GetBookingsUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.LoadMoreBookingsUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.RefreshBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val refreshBookingsUseCase: RefreshBookingsUseCase,
    private val loadMoreBookingsUseCase: LoadMoreBookingsUseCase
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isLoadMoreLoading = MutableStateFlow(false)
    val isLoadMoreLoading = _isLoadMoreLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val bookings = getBookingsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = refreshBookingsUseCase()) {
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadMore() {
        if (_isLoadMoreLoading.value || _isLoading.value) return
        viewModelScope.launch {
            _isLoadMoreLoading.value = true
            val currentSize = bookings.value.size
            when (val result = loadMoreBookingsUseCase(startIndex = currentSize, count = 10)) {
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoadMoreLoading.value = false
        }
    }
}