package com.uol.userapp.core.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Responsável exclusivamente por construir a infraestrutura de rede
 * (OkHttpClient + Retrofit).
 *
 * Segue o Single Responsibility Principle: esta classe não conhece nada
 * sobre os endpoints da aplicação (isso é responsabilidade de [ApiService]),
 * nem sobre quem consome o Retrofit.
 */
object RetrofitClient {

    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    private const val TIMEOUT_SECONDS = 30L

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instância única (singleton) do Retrofit, pronta para criar
     * qualquer interface de serviço (ex.: [ApiService]).
     */
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}