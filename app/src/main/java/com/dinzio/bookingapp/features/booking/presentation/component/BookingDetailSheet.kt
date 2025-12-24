package com.dinzio.bookingapp.features.booking.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.presentation.viewModel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailSheet(
    bookingId: Int,
    viewModel: BookingViewModel,
    onDismiss: () -> Unit
) {
    LaunchedEffect(bookingId) {
        viewModel.getBookingDetail(bookingId)
    }

    val detailState by viewModel.bookingDetail.collectAsState()

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.clearDetailState()
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                "Booking Detail",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = detailState) {
                is Resource.Loading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    val data = state.data!!
                    DetailItem(label = "Guest Name", value = "${data.firstname} ${data.lastname}")
                    DetailItem(label = "Booking ID", value = "#${data.bookingid}")
                    DetailItem(label = "Stay Period", value = "${data.checkin} - ${data.checkout}")
                    DetailItem(label = "Total Price", value = "IDR ${data.totalprice}")
                    DetailItem(
                        label = "Notes",
                        value = data.additionalneeds?.ifBlank { "-" } ?: "-")
                }

                is Resource.Error -> {
                    Text(
                        "Gagal memuat data: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {}
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}