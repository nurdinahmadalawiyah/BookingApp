package com.dinzio.bookingapp.features.booking.data.remote.source

import com.dinzio.bookingapp.features.booking.data.remote.model.BookingDetailResponse
import com.dinzio.bookingapp.features.booking.data.remote.model.BookingIdResponse
import com.dinzio.bookingapp.features.booking.data.remote.model.BookingRequest
import com.dinzio.bookingapp.features.booking.data.remote.model.BookingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingApiService {
    @GET("booking")
    suspend fun getBookingIds(
        @Query("firstname") firstname: String? = null,
        @Query("lastname") lastname: String? = null
    ): List<BookingIdResponse>

    @Headers("Accept: application/json")
    @GET("booking/{id}")
    suspend fun getBookingDetail(@Path("id") id: Int): BookingDetailResponse

    @Headers("Accept: application/json")
    @POST("booking")
    suspend fun createBooking(@Body booking: BookingRequest): BookingResponse
}