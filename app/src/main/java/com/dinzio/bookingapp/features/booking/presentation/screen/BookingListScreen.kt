package com.dinzio.bookingapp.features.booking.presentation.screen

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.presentation.component.BookingDetailSheet
import com.dinzio.bookingapp.features.booking.presentation.component.BookingItem
import com.dinzio.bookingapp.features.booking.presentation.component.BookingSuccessDialog
import com.dinzio.bookingapp.features.booking.presentation.component.CreateBookingSheet
import com.dinzio.bookingapp.features.booking.presentation.component.DeleteConfirmationDialog
import com.dinzio.bookingapp.features.booking.presentation.viewModel.BookingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    viewModel: BookingViewModel = hiltViewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val token by viewModel.authToken.collectAsState()

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedBooking by remember { mutableStateOf<BookingEntity?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateSheet by remember { mutableStateOf(false) }

    val createSuccessData by viewModel.createBookingSuccess.collectAsState()

    var showEditSheet by remember { mutableStateOf(false) }
    var bookingToEdit by remember { mutableStateOf<BookingEntity?>(null) }

    val isUpdateSuccess by viewModel.updateBookingSuccess.collectAsState()
    val updatedData by viewModel.updatedData.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    val isDeleteSuccess by viewModel.deleteBookingSuccess.collectAsState()

    LaunchedEffect(error) {
        error.let {
            scope.launch {
                snackbarHostState.showSnackbar("Error: $error")
            }
        }
    }

    LaunchedEffect(error) {
        error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
             viewModel.clearError()
        }
    }

    LaunchedEffect(isDeleteSuccess) {
        if (isDeleteSuccess) {
            showSheet = false
            snackbarHostState.showSnackbar("Booking deleted successfully")
            viewModel.resetDeleteState()
        }
    }

    if (isUpdateSuccess && updatedData != null) {
        BookingSuccessDialog(
            booking = updatedData!!,
            onConfirm = {
                viewModel.resetUpdateState()
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                selectedBooking?.let { viewModel.deleteBooking(it.bookingid) }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showSheet && selectedBooking != null) {
        BookingDetailSheet(
            bookingId = selectedBooking!!.bookingid,
            viewModel = viewModel,
            onEditClick = { booking ->
                bookingToEdit = booking
                showSheet = false
                showEditSheet = true
            },
            onDismiss = { showSheet = false },
            onDeleteClick = { showDeleteDialog = true }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Booking Explorer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
            if (showSheet && selectedBooking != null) {
                selectedBooking?.bookingid?.let {
                    BookingDetailSheet(
                        bookingId = it,
                        viewModel = viewModel,
                        onEditClick = { booking ->
                            bookingToEdit = booking
                            showSheet = false
                            showEditSheet = true
                        },
                        onDismiss = { showSheet = false },
                        onDeleteClick = { showDeleteDialog = true }
                    )
                }
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

            createSuccessData?.let { booking ->
                BookingSuccessDialog(
                    booking = booking,
                    onConfirm = {
                        viewModel.clearCreateSuccessState()
                        viewModel.resetUpdateState()
                    }
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search by First Name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
            ) {
                if (bookings.isEmpty() && !isLoading) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            val infiniteTransition = rememberInfiniteTransition(label = "iconScale")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.5f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "No bookings found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Try to search using first name or check your internet connection.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )

                            Log.d("UI_DEBUG", "Token saat ini di UI: $token")
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(bookings, key = { it.bookingid }) { booking ->
                            BookingItem(
                                booking = booking,
                                onClick = {
                                    selectedBooking = booking
                                    showSheet = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}