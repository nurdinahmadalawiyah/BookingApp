package com.dinzio.bookingapp.core.di

import com.dinzio.bookingapp.features.auth.data.repository.AuthRepositoryImpl
import com.dinzio.bookingapp.features.auth.domain.repository.AuthRepository
import com.dinzio.bookingapp.features.booking.data.remote.repository.BookingRepositoryImpl
import com.dinzio.bookingapp.features.booking.domain.repository.BookingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository
}