package com.dinzio.bookingapp.features.booking.data.remote.repository

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.common.utils.safeApiCall
import com.dinzio.bookingapp.features.booking.data.local.dao.BookingDao
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import com.dinzio.bookingapp.features.booking.data.remote.source.BookingApiService
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val apiService: BookingApiService,
    private val bookingDao: BookingDao
) : BookingRepository {

    override fun getBookingsFromDb(): Flow<List<BookingEntity>> {
        return bookingDao.getAllBookings()
    }

    override suspend fun refreshBookings(): Resource<Unit> {
        return loadAndSaveBookings(startIndex = 0, count = 10, shouldDeleteOld = true)
    }

    override suspend fun loadMoreBookings(startIndex: Int, count: Int): Resource<Unit> {
        return loadAndSaveBookings(startIndex = startIndex, count = count, shouldDeleteOld = false)
    }

    private suspend fun loadAndSaveBookings(
        startIndex: Int,
        count: Int,
        shouldDeleteOld: Boolean
    ): Resource<Unit> {
        return safeApiCall {
            val responseIds = apiService.getBookingIds()

            val idsToFetch = responseIds.drop(startIndex).take(count)
            val bookingEntities = mutableListOf<BookingEntity>()

            for (item in idsToFetch) {
                try {
                    val detail = apiService.getBookingDetail(item.bookingid)
                    bookingEntities.add(
                        BookingEntity(
                            bookingid = item.bookingid,
                            firstname = detail.firstname,
                            lastname = detail.lastname,
                            totalprice = detail.totalprice,
                            depositpaid = detail.depositpaid,
                            checkin = detail.bookingdates.checkin,
                            checkout = detail.bookingdates.checkout,
                            additionalneeds = detail.additionalneeds ?: ""
                        )
                    )
                    delay(500)
                } catch (e: Exception) {
                    continue
                }
            }

            if (bookingEntities.isNotEmpty()) {
                if (shouldDeleteOld) {
                    bookingDao.deleteAllBookings()
                }
                bookingDao.insertBookings(bookingEntities)
            }
        }
    }
}