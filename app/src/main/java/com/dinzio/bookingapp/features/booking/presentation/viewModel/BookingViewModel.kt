package com.dinzio.bookingapp.features.booking.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.core.data.local.TokenManager
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.domain.usecase.CreateBookingUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.DeleteBookingUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.GetBookingDetailUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.GetBookingsUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.RefreshBookingsUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.UpdateBookingUseCase
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
    private val getBookingDetailUseCase: GetBookingDetailUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val deleteBookingUseCase: DeleteBookingUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _bookingDetail = MutableStateFlow<Resource<BookingEntity>?>(null)
    val bookingDetail = _bookingDetail.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _createBookingSuccess = MutableStateFlow<BookingEntity?>(null)
    val createBookingSuccess = _createBookingSuccess.asStateFlow()

    private val _updateBookingSuccess = MutableStateFlow<Boolean>(false)
    val updateBookingSuccess = _updateBookingSuccess.asStateFlow()

    private val _updatedData = MutableStateFlow<BookingEntity?>(null)
    val updatedData = _updatedData.asStateFlow()

    private val _deleteBookingSuccess = MutableStateFlow(false)
    val deleteBookingSuccess = _deleteBookingSuccess.asStateFlow()

    val authToken = tokenManager.getToken().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

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

    fun createBooking(entity: BookingEntity) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = createBookingUseCase(entity)) {
                is Resource.Success -> {
                    _createBookingSuccess.value = result.data
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearCreateSuccessState() {
        _createBookingSuccess.value = null
    }

    fun updateBooking(entity: BookingEntity) {
        val token = authToken.value

        if (token == null) {
            _error.value = "Token not found, please login again"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when(val result = updateBookingUseCase(token, entity)) {
                is Resource.Success -> {
                    _updatedData.value = result.data
                    _updateBookingSuccess.value = true
                    _bookingDetail.value = Resource.Success(result.data!!)
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun resetUpdateState() {
        _updateBookingSuccess.value = false
        _updatedData.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun deleteBooking(id: Int) {
        val token = authToken.value

        if (token == null) {
            _error.value = "Token not found, please login again"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = deleteBookingUseCase(token, id)) {
                is Resource.Success -> {
                    _deleteBookingSuccess.value = true
                }
                is Resource.Error -> {
                    _error.value = "Failed Deleted: ${result.message}"
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun resetDeleteState() {
        _deleteBookingSuccess.value = false
    }
}