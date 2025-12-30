package com.dinzio.bookingapp.features.booking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dinzio.bookingapp.features.booking.data.local.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY bookingid DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Query("DELETE FROM bookings WHERE isSynced = 1")
    suspend fun deleteAllBookings()

    @Query("DELETE FROM bookings WHERE bookingid = :bookingId")
    suspend fun deleteBookingById(bookingId: Int)

    @Query("SELECT * FROM bookings WHERE isSynced = 0")
    suspend fun getUnsyncedBookings(): List<BookingEntity>

    @Query("UPDATE bookings SET isSynced = 1 WHERE bookingid = :id")
    suspend fun markAsSynced(id: Int)
}