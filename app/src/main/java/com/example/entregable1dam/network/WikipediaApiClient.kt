package com.example.entregable1dam.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WikipediaApiClient {
    private const val BASE_URL = "https://es.wikipedia.org/"

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "ITANES-Android-Educational-App/1.0")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: ItanesApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ItanesApi::class.java)
}
