package com.dinzio.bookingapp.core.di

import android.content.Context
import androidx.room.Room
import com.dinzio.bookingapp.core.data.local.AppDatabase
import com.dinzio.bookingapp.features.booking.data.local.dao.BookingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "booking_db"
        ).build()
    }

    @Provides
    fun provideBookingDao(db: AppDatabase): BookingDao {
        return db.bookingDao()
    }
}