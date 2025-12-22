package com.dinzio.bookingapp.features.auth.data.repository

import com.dinzio.bookingapp.common.network.Resource
import com.dinzio.bookingapp.common.utils.safeApiCall
import com.dinzio.bookingapp.core.data.local.TokenManager
import com.dinzio.bookingapp.features.auth.data.source.AuthApiService
import com.dinzio.bookingapp.features.auth.data.model.AuthRequest
import com.dinzio.bookingapp.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {
    override fun login(username: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall {
            val response = apiService.login(AuthRequest(username, password))
            response.token
        }
        if (result is Resource.Success) {
            result.data?.let { tokenManager.saveToken(it) }
        }
        emit(result)
    }
}