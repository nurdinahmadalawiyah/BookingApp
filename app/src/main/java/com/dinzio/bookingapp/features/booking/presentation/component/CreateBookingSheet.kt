package com.dinzio.bookingapp.features.booking.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.presentation.viewModel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingSheet(
    viewModel: BookingViewModel,
    bookingEntity: BookingEntity? = null,
    onDismiss: () -> Unit
) {
    var firstName by remember { mutableStateOf(bookingEntity?.firstname ?: "") }
    var lastName by remember { mutableStateOf(bookingEntity?.lastname ?: "") }
    var price by remember { mutableStateOf(bookingEntity?.totalprice?.toString() ?: "") }
    var checkin by remember { mutableStateOf(bookingEntity?.checkin ?: "") }
    var checkout by remember { mutableStateOf(bookingEntity?.checkout ?: "") }
    var additionalNeeds by remember { mutableStateOf(bookingEntity?.additionalneeds ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                 if (bookingEntity == null) "Create New Booking" else "Edit Booking",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        price = input
                    }
                },
                label = { Text("Total Price") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                prefix = { Text("$ ") },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DatePickerField(label = "Check-in", selectedDate = checkin) { checkin = it }
                }
                Box(modifier = Modifier.weight(1f)) {
                    DatePickerField(label = "Check-out", selectedDate = checkout) { checkout = it }
                }
            }

            OutlinedTextField(
                value = additionalNeeds,
                onValueChange = { additionalNeeds = it },
                label = { Text("Additional Needs (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    val updatedBooking = BookingEntity(
                        bookingid = bookingEntity?.bookingid ?: 0,
                        firstname = firstName,
                        lastname = lastName,
                        totalprice = price.toIntOrNull() ?: 0,
                        depositpaid = true,
                        checkin = checkin,
                        checkout = checkout,
                        additionalneeds = additionalNeeds
                    )
                    if (bookingEntity == null) {
                        viewModel.createBooking(updatedBooking)
                    } else {
                        viewModel.updateBooking(updatedBooking)
                    }
                    onDismiss()
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && price.isNotBlank() && checkin.isNotBlank() && checkout.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (bookingEntity == null) "Confirm Booking" else "Save Changes")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}