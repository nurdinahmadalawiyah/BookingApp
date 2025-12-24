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

    override suspend fun refreshBookings(query: String): Resource<Unit> {
        return loadAndSaveBookings(startIndex = 0, count = 10, shouldDeleteOld = true, query = query)
    }

    override suspend fun getBookingDetailById(id: Int): Resource<BookingEntity> {
        return safeApiCall {
            val detail = apiService.getBookingDetail(id)
            val entity = BookingEntity(
                bookingid = id,
                firstname = detail.firstname,
                lastname = detail.lastname,
                totalprice = detail.totalprice,
                depositpaid = detail.depositpaid,
                checkin = detail.bookingdates.checkin,
                checkout = detail.bookingdates.checkout,
                additionalneeds = detail.additionalneeds ?: ""
            )

            bookingDao.insertBookings(listOf(entity))

            entity
        }
    }

    private suspend fun loadAndSaveBookings(
        startIndex: Int,
        count: Int,
        shouldDeleteOld: Boolean,
        query: String
    ): Resource<Unit> {
        return safeApiCall {
            val responseIds = apiService.getBookingIds(firstname = query)
            val idsToFetch = responseIds.drop(startIndex).take(count)

            val bookingEntities = idsToFetch.map { item ->
                BookingEntity(
                    bookingid = item.bookingid,
                    firstname = "",
                    lastname = "",
                    totalprice = 0,
                    depositpaid = false,
                    checkin = "",
                    checkout = "",
                    additionalneeds = ""
                )
            }

            if (shouldDeleteOld) {
                bookingDao.deleteAllBookings()
            }

            if (bookingEntities.isNotEmpty()) {
                bookingDao.insertBookings(bookingEntities)
            }

            Resource.Success(Unit)
        }
    }
}