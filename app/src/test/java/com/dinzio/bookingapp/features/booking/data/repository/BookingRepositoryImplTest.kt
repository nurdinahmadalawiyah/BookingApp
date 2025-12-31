package com.dinzio.bookingapp.features.booking.data.repository

import app.cash.turbine.test
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.data.local.dao.BookingDao
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.data.remote.model.BookingDates
import com.dinzio.bookingapp.features.booking.data.remote.model.BookingDetailResponse
import com.dinzio.bookingapp.features.booking.data.remote.model.BookingIdResponse
import com.dinzio.bookingapp.features.booking.data.remote.model.BookingResponse
import com.dinzio.bookingapp.features.booking.data.remote.repository.BookingRepositoryImpl
import com.dinzio.bookingapp.features.booking.data.remote.source.BookingApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BookingRepositoryImplTest {
    private val apiService: BookingApiService = mockk()
    private val bookingDao: BookingDao = mockk(relaxed = true)
    private val repository = BookingRepositoryImpl(apiService, bookingDao)

    @Test
    fun `getBookingsFromDb should return flow of list from dao`() = runTest {
        // Arrange
        val mockList = listOf(
            BookingEntity(
                bookingid = 1,
                firstname = "Dinzio",
                lastname = "Dev",
                totalprice = 100,
                depositpaid = true,
                checkin = "",
                checkout = "",
                additionalneeds = ""
            )
        )
        every { bookingDao.getAllBookings() } returns flowOf(mockList)

        // Act
        repository.getBookingsFromDb().test {
            // Assert
            assertEquals(mockList, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `refreshBookings success should fetch from api and save to dao`() = runTest {
        // Arrange
        val mockIds = listOf(BookingIdResponse(101))
        coEvery { apiService.getBookingIds(firstname = "") } returns mockIds

        // Act
        val result = repository.refreshBookings("")

        // Assert
        assert(result is Resource.Success)
        coVerify { bookingDao.deleteAllBookings() }
        coVerify { bookingDao.insertBookings(any()) }
    }

    @Test
    fun `createBooking success should call api and save to dao`() = runTest {
        // Arrange
        val entity = BookingEntity(
            bookingid = 0,
            firstname = "John",
            lastname = "Doe",
            totalprice = 100,
            depositpaid = true,
            checkin = "2023-12-01",
            checkout = "2023-12-02",
            additionalneeds = "None"
        )

        val mockResponse = BookingResponse(
            bookingid = 123,
            booking = BookingDetailResponse(
                firstname = "John",
                lastname = "Doe",
                totalprice = 100,
                depositpaid = true,
                bookingdates = BookingDates("2023-12-01", "2023-12-02"),
                additionalneeds = "None"
            )
        )

        coEvery { apiService.createBooking(any()) } returns mockResponse

        // Act
        val result = repository.createBooking(entity)

        // Assert
        assert(result is Resource.Success)
        assertEquals(123, (result as Resource.Success).data?.bookingid)

        coVerify { apiService.createBooking(any()) }
        coVerify { bookingDao.insertBookings(any<List<BookingEntity>>()) }
    }
}