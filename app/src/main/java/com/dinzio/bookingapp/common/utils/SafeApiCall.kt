package com.dinzio.bookingapp.common.utils

import com.dinzio.bookingapp.common.network.Resource
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
    return try {
        val response = apiCall()
        Resource.Success(response)
    } catch (throwable: Throwable) {
        when (throwable) {
            is IOException -> Resource.Error("Masalah koneksi internet")
            is HttpException -> {
                val message = when (throwable.code()) {
                    401 -> "Username atau password salah (401)"
                    else -> "Kesalahan Server (${throwable.code()})"
                }
                Resource.Error(message)
            }
            else -> Resource.Error(throwable.message ?: "Kesalahan tidak dikenal")
        }
    }
}