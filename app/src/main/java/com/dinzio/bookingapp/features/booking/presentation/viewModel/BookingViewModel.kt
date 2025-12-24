package com.dinzio.bookingapp.features.booking.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.usecase.GetBookingDetailUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.GetBookingsUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.RefreshBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val refreshBookingsUseCase: RefreshBookingsUseCase,
    private val getBookingDetailUseCase: GetBookingDetailUseCase
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _bookingDetail = MutableStateFlow<Resource<BookingEntity>?>(null)
    val bookingDetail = _bookingDetail.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

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

            when (val result = refreshBookingsUseCase(query = searchQuery.value)) {
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun getBookingDetail(id: Int) {
        viewModelScope.launch {
            _bookingDetail.value = Resource.Loading()
            _bookingDetail.value = getBookingDetailUseCase(id)
        }
    }

    fun clearDetailState() {
        _bookingDetail.value = null
    }

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(700)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            refreshBookingsUseCase(query)
            _isLoading.value = false
        }
    }
}