package com.xtremeiptv.core.network.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xtremeiptv.core.network.api.MacPortalApi
import com.xtremeiptv.core.network.api.StalkerPortalApi
import com.xtremeiptv.core.network.api.XtreamCodesApi
import com.xtremeiptv.core.network.interceptor.AuthInterceptor
import com.xtremeiptv.core.network.interceptor.LoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: LoggingInterceptor
    ): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.xtremeiptv.com/") // Base URL, will be overridden per request
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideXtreamCodesApi(retrofit: Retrofit): XtreamCodesApi {
        return retrofit.create(XtreamCodesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStalkerPortalApi(retrofit: Retrofit): StalkerPortalApi {
        return retrofit.create(StalkerPortalApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMacPortalApi(retrofit: Retrofit): MacPortalApi {
        return retrofit.create(MacPortalApi::class.java)
    }
}
