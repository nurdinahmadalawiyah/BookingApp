package com.dinzio.bookingapp.features.booking.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val bookingid: Int,
    val firstname: String,
    val lastname: String,
    val totalprice: Int,
    val depositpaid: Boolean,
    val checkin: String,
    val checkout: String,
    val additionalneeds: String?
)