package com.dinzio.bookingapp.features.booking.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dinzio.bookingapp.features.auth.presentation.viewModel.LoginViewModel
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.presentation.component.BookingDetailSheet
import com.dinzio.bookingapp.features.booking.presentation.component.BookingItem
import com.dinzio.bookingapp.features.booking.presentation.component.BookingSuccessDialog
import com.dinzio.bookingapp.features.booking.presentation.component.CreateBookingSheet
import com.dinzio.bookingapp.features.booking.presentation.component.DeleteConfirmationDialog
import com.dinzio.bookingapp.features.booking.presentation.component.EmptyBookingComponent
import com.dinzio.bookingapp.features.booking.presentation.component.LogoutConfirmationDialog
import com.dinzio.bookingapp.features.booking.presentation.viewModel.BookingUiEvent
import com.dinzio.bookingapp.features.booking.presentation.viewModel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    viewModel: BookingViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val bookings by viewModel.bookings.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var successDialogData by remember { mutableStateOf<BookingEntity?>(null) }

    var selectedBooking by remember { mutableStateOf<BookingEntity?>(null) }
    var bookingToEdit by remember { mutableStateOf<BookingEntity?>(null) }

    var showDetailSheet by remember { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }


    /* ---------------- EVENT HANDLER ---------------- */

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is BookingUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is BookingUiEvent.BookingCreated -> {
                    showCreateSheet = false
                    successDialogData = event.data
                }

                is BookingUiEvent.BookingUpdated -> {
                    showEditSheet = false
                    successDialogData = event.data
                }

                is BookingUiEvent.BookingDeleted -> {
                    showDetailSheet = false
                    snackbarHostState.showSnackbar("Booking deleted successfully")
                }
            }
        }
    }

    /* ---------------- DIALOGS & SHEETS ---------------- */

    if (showDetailSheet && selectedBooking != null) {
        BookingDetailSheet(
            bookingId = selectedBooking!!.bookingid,
            viewModel = viewModel,
            onEditClick = {
                bookingToEdit = it
                showDetailSheet = false
                showEditSheet = true
            },
            onDismiss = { showDetailSheet = false },
            onDeleteClick = { showDeleteDialog = true }
        )
    }

    if (showCreateSheet) {
        CreateBookingSheet(
            viewModel = viewModel,
            onDismiss = { showCreateSheet = false }
        )
    }

    if (showEditSheet && bookingToEdit != null) {
        CreateBookingSheet(
            viewModel = viewModel,
            bookingEntity = bookingToEdit,
            onDismiss = {
                showEditSheet = false
                bookingToEdit = null
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                selectedBooking?.let {
                    viewModel.deleteBooking(it.bookingid)
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                loginViewModel.logout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    successDialogData?.let { booking ->
        BookingSuccessDialog(
            booking = booking,
            onConfirm = {
                successDialogData = null
            }
        )
    }

    /* ---------------- UI ---------------- */

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Booking Explorer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Booking")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            /* ---------------- SEARCH ---------------- */

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search by First Name...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            /* ---------------- LIST / EMPTY ---------------- */

            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
            ) {
                when {
                    bookings.isEmpty() && !uiState.isLoading -> {
                        EmptyBookingComponent()
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(bookings, key = { it.bookingid }) { booking ->
                                BookingItem(
                                    booking = booking,
                                    onClick = {
                                        selectedBooking = booking
                                        showDetailSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
