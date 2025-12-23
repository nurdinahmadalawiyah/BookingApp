package com.dinzio.bookingapp.features.booking.data.remote.source

import com.dinzio.bookingapp.features.booking.data.remote.model.BookingDetailResponse
import com.dinzio.bookingapp.features.booking.data.remote.model.BookingIdResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface BookingApiService {
    @GET("booking")
    suspend fun getBookingIds(): List<BookingIdResponse>

    @Headers("Accept: application/json")
    @GET("booking/{id}")
    suspend fun getBookingDetail(@Path("id") id: Int): BookingDetailResponse
}