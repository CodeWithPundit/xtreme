package com.xtremeiptv.core.network.interceptor

import com.xtremeiptv.core.common.manager.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Add authentication headers if needed
        val requestBuilder = originalRequest.newBuilder()
        
        sessionManager.getActiveProfile()?.let { profile ->
            when (profile.protocolType) {
                ProtocolType.XTREAM_CODES -> {
                    val config = profile.protocolConfig as ProtocolConfig.XtreamCodes
                    // Add Xtream Codes auth params to URL
                    val url = originalRequest.url.newBuilder()
                        .addQueryParameter("username", config.username)
                        .addQueryParameter("password", config.password)
                        .build()
                    requestBuilder.url(url)
                }
                ProtocolType.STALKER_PORTAL -> {
                    sessionManager.getStalkerToken()?.let { token ->
                        requestBuilder.addHeader("Cookie", "PHPSESSID=$token")
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                }
                ProtocolType.MAC_PORTAL -> {
                    val config = profile.protocolConfig as ProtocolConfig.MACPortal
                    config.headers.forEach { (key, value) ->
                        requestBuilder.addHeader(key, value)
                    }
                }
                else -> {}
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
