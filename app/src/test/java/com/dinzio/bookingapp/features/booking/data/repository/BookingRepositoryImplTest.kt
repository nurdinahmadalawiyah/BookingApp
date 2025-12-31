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
    fun `getBookingDetailById success should fetch from api and save to dao`() = runTest {
        // Arrange
        val bookingId = 123
        val mockDetailResponse = BookingDetailResponse(
            firstname = "John",
            lastname = "Doe",
            totalprice = 100,
            depositpaid = true,
            bookingdates = BookingDates("2023-12-01", "2023-12-02"),
            additionalneeds = "Late checkout"
        )

        coEvery { apiService.getBookingDetail(bookingId) } returns mockDetailResponse

        // Act
        val result = repository.getBookingDetailById(bookingId)

        // Assert
        assert(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals("John", data?.firstname)
        assertEquals(bookingId, data?.bookingid)

        coVerify { bookingDao.insertBookings(any()) }
    }

    @Test
    fun `getBookingDetailById failure should return error resource`() = runTest {
        // Arrange
        val bookingId = 999
        val errorMsg = "HTTP 404 Not Found"
        coEvery { apiService.getBookingDetail(bookingId) } throws Exception(errorMsg)

        // Act
        val result = repository.getBookingDetailById(bookingId)

        // Assert
        assert(result is Resource.Error)
        assertEquals(errorMsg, (result as Resource.Error).message)
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

    @Test
    fun `updateBooking success should call api and save to dao`() = runTest {
        val bookingId = 123
        val entity = BookingEntity(
            bookingid = bookingId,
            firstname = "John",
            lastname = "Doe",
            totalprice = 100,
            depositpaid = true,
            checkin = "2023-01-01",
            checkout = "2023-01-02",
            additionalneeds = "WiFi"
        )

        val mockApiResponse = BookingDetailResponse(
            firstname = "John",
            lastname = "Doe",
            totalprice = 100,
            depositpaid = true,
            bookingdates = BookingDates("2023-01-01", "2023-01-02"),
            additionalneeds = "WiFi"
        )

        coEvery {
            apiService.updateBooking(any(), any(), any())
        } returns mockApiResponse

        coEvery { bookingDao.insertBookings(any()) } returns Unit

        val result = repository.updateBooking("mock_token", entity)

        assert(result is Resource.Success)
        coVerify { apiService.updateBooking(eq(bookingId), any(), any()) }
        coVerify { bookingDao.insertBookings(any()) }
    }

    @Test
    fun `updateBooking failure should return error resource`() = runTest {
        // Arrange
        val token = "invalid_token"
        val entity = BookingEntity(
            bookingid = 1,
            firstname = "X",
            lastname = "Y",
            totalprice = 0,
            depositpaid = false,
            checkin = "",
            checkout = "",
            additionalneeds = ""
        )

        coEvery { apiService.updateBooking(any(), any(), any()) } throws Exception("Unauthorized")

        // Act
        val result = repository.updateBooking(token, entity)

        // Assert
        assert(result is Resource.Error)
        assertEquals("Unauthorized", (result as Resource.Error).message)
    }
}