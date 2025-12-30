package com.dinzio.bookingapp.features.booking.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.features.booking.data.local.dao.BookingDao
import com.dinzio.bookingapp.features.booking.domain.usecase.CreateBookingUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncBookingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val createBookingUseCase: CreateBookingUseCase,
    private val bookingDao: BookingDao
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val unsyncedBookings = bookingDao.getUnsyncedBookings()

            if (unsyncedBookings.isEmpty()) return Result.success()

            unsyncedBookings.forEach { entity ->
                val result = createBookingUseCase(entity)

                if (result is Resource.Success) {
                    bookingDao.deleteBookingById(entity.bookingid)
                    bookingDao.insertBookings(listOf(result.data!!.copy(isSynced = true)))
                } else {
                    return Result.retry();
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

}