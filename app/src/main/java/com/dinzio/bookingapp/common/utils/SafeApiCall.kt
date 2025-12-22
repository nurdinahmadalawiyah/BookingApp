package com.dinzio.bookingapp.common.utils

import com.dinzio.bookingapp.common.network.Resource
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(apiCall())
    } catch (throwable: Throwable) {
        when (throwable) {
            is IOException -> Resource.Error("Masalah koneksi internet")
            is HttpException -> {
                val code = throwable.code()
                val message = when (code) {
                    401 -> "Sesi berakhir atau password salah (401)"
                    403 -> "Anda tidak memiliki akses (403)"
                    404 -> "Data tidak ditemukan (404)"
                    500 -> "Server sedang bermasalah (500)"
                    else -> "Terjadi kesalahan: $code"
                }
                Resource.Error(message)
            }
            else -> Resource.Error("Kesalahan tidak dikenal: ${throwable.message}")
        }
    }
}