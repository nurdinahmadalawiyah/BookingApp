package com.dinzio.bookingapp.features.auth.domain.repository

import com.dinzio.bookingapp.common.network.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(username: String, password: String): Flow<Resource<String>>
}