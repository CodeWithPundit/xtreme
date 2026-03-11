package com.xtremeiptv.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()
        
        // Log request
        Timber.d("Request: ${request.method} ${request.url}")
        request.headers.forEach { header ->
            Timber.d("Header: ${header.first}: ${header.second}")
        }
        
        val response = chain.proceed(request)
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000
        
        // Log response
        Timber.d("Response: ${response.code} (${durationMs}ms) for ${request.url}")
        
        return response
    }
}
