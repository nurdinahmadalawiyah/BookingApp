package com.dinzio.bookingapp.features.booking.data.remote.model

import com.google.gson.annotations.SerializedName

data class BookingIdResponse(
    @SerializedName("bookingid") val bookingid: Int
)

data class BookingDetailResponse(
    @SerializedName("firstname") val firstname: String,
    @SerializedName("lastname") val lastname: String,
    @SerializedName("totalprice") val totalprice: Int,
    @SerializedName("depositpaid") val depositpaid: Boolean,
    @SerializedName("bookingdates") val bookingdates: BookingDates,
    @SerializedName("additionalneeds") val additionalneeds: String?
)

data class BookingDates(
    @SerializedName("checkin") val checkin: String,
    @SerializedName("checkout") val checkout: String
)

data class BookingResponse(
    @SerializedName("bookingid") val bookingid: Int,
    @SerializedName("booking") val booking: BookingDetailResponse
)

data class BookingRequest(
    @SerializedName("firstname") val firstname: String,
    @SerializedName("lastname") val lastname: String,
    @SerializedName("totalprice") val totalprice: Int,
    @SerializedName("depositpaid") val depositpaid: Boolean,
    @SerializedName("bookingdates") val bookingdates: BookingDates,
    @SerializedName("additionalneeds") val additionalneeds: String?
)