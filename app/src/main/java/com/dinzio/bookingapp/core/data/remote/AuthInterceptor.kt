package com.dinzio.bookingapp.core.data.remote

import com.dinzio.bookingapp.core.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val dataStore: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            dataStore.getToken().first()
        }

        val request = chain.request().newBuilder()

        token?.let {
            request.addHeader("Cookie", "token=$it")
        }

        return chain.proceed(request.build())
    }
}