package com.homeops.grocery.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provides a lazily-rebuilt [ApiService] that respects a runtime-configured
 * BASE_URL.  Call [getApi] with the current URL from SettingsManager before
 * every request; a new Retrofit instance is created only when the URL changes.
 */
object RetrofitClient {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @Volatile private var currentBaseUrl: String = ""
    @Volatile private var cachedApi: ApiService? = null

    /**
     * Returns an [ApiService] configured for [baseUrl].
     * Rebuilds the Retrofit instance only when the URL has changed.
     */
    fun getApi(baseUrl: String): ApiService {
        if (baseUrl == currentBaseUrl && cachedApi != null) return cachedApi!!
        return synchronized(this) {
            if (baseUrl != currentBaseUrl || cachedApi == null) {
                currentBaseUrl = baseUrl
                cachedApi = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            cachedApi!!
        }
    }
}
