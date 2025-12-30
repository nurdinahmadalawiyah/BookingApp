package com.dinzio.bookingapp.features.booking.presentation.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.core.data.local.TokenManager
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.data.worker.SyncBookingWorker
import com.dinzio.bookingapp.features.booking.domain.usecase.CreateBookingUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.DeleteBookingUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.GetBookingDetailUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.GetBookingsUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.RefreshBookingsUseCase
import com.dinzio.bookingapp.features.booking.domain.usecase.UpdateBookingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    getBookingsUseCase: GetBookingsUseCase,
    private val refreshBookingsUseCase: RefreshBookingsUseCase,
    private val getBookingDetailUseCase: GetBookingDetailUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val deleteBookingUseCase: DeleteBookingUseCase,
    private val tokenManager: TokenManager,
    private val workManager: WorkManager
) : ViewModel() {

    private val _state = MutableStateFlow(BookingUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<BookingUiEvent>()
    val event = _event.asSharedFlow()

    private val authToken = tokenManager.getToken()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val bookings = getBookingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        observeSearch()
        refresh()
        scheduleOfflineSync()
    }

    /* ---------------- CORE HELPERS ---------------- */

    private inline fun reduce(block: BookingUiState.() -> BookingUiState) {
        _state.update(block)
    }

    private fun requireToken(): String? {
        return authToken.value ?: run {
            emitEvent(BookingUiEvent.ShowError("Token expired. Please login again."))
            null
        }
    }

    private fun emitEvent(event: BookingUiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    /* ---------------- SEARCH ---------------- */

    fun onSearchQueryChange(query: String) {
        reduce { copy(searchQuery = query) }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            state
                .map { it.searchQuery }
                .debounce(700)
                .distinctUntilChanged()
                .collect { refreshBookingsUseCase(it) }
        }
    }

    /* ---------------- ACTIONS ---------------- */

    fun refresh() {
        viewModelScope.launch {
            reduce { copy(isLoading = true, error = null) }

            val result = refreshBookingsUseCase(state.value.searchQuery)
            if (result is Resource.Error) {
                reduce { copy(error = result.message) }
            }

            reduce { copy(isLoading = false) }
        }
    }

    fun getBookingDetail(id: Int) {
        viewModelScope.launch {
            reduce { copy(isLoading = true) }

            when (val result = getBookingDetailUseCase(id)) {
                is Resource.Success ->
                    reduce { copy(bookingDetail = result.data) }

                is Resource.Error ->
                    emitEvent(BookingUiEvent.ShowError(result.message ?: "Unknown error"))

                else -> Unit
            }

            reduce { copy(isLoading = false) }
        }
    }

    fun createBooking(entity: BookingEntity) {
        viewModelScope.launch {
            reduce { copy(isLoading = true) }

            val offlineBooking = entity.copy(isSynced = false)

            when (val result = createBookingUseCase(offlineBooking)) {
                is Resource.Success ->
                    emitEvent(BookingUiEvent.BookingCreated(result.data!!))

                is Resource.Error -> {
                    scheduleOfflineSync()
                    emitEvent(BookingUiEvent.ShowError(result.message ?: "Create failed"))
                }
                else -> Unit
            }

            reduce { copy(isLoading = false) }
        }
    }

    fun updateBooking(entity: BookingEntity) {
        val token = requireToken() ?: return

        viewModelScope.launch {
            reduce { copy(isLoading = true) }

            when (val result = updateBookingUseCase(token, entity)) {
                is Resource.Success -> {
                    reduce { copy(bookingDetail = result.data) }
                    emitEvent(BookingUiEvent.BookingUpdated(result.data!!))
                }

                is Resource.Error ->
                    emitEvent(BookingUiEvent.ShowError(result.message ?: "Update failed"))

                else -> Unit
            }

            reduce { copy(isLoading = false) }
        }
    }

    fun deleteBooking(id: Int) {
        val token = requireToken() ?: return

        viewModelScope.launch {
            reduce { copy(isLoading = true) }

            when (val result = deleteBookingUseCase(token, id)) {
                is Resource.Success ->
                    emitEvent(BookingUiEvent.BookingDeleted)

                is Resource.Error ->
                    emitEvent(BookingUiEvent.ShowError(result.message ?: "Delete failed"))

                else -> Unit
            }

            reduce { copy(isLoading = false) }
        }
    }

    fun scheduleOfflineSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncBookingWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_bookings",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
}
