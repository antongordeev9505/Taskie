package com.raywenderlich.android.taskie.networking

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor

//default client
fun buildClient(): OkHttpClient =
    OkHttpClient.Builder()
            //added interceptor, to intercept on BODY level
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

//make more forgiving parser
private var json = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

//setup retrofit using client and baseURL
@ExperimentalSerializationApi
fun buildRetrofit(): Retrofit {
    //create media type object from a string
    val contentType = "application/json".toMediaType()

    return Retrofit.Builder()
        .client(buildClient())
            // use kotlin serialization as a converter
            //ConverterFactory will parse everything automatically
        .addConverterFactory(json.asConverterFactory(contentType))
        .baseUrl(BASE_URL)
        .build()
}

//build the API service
@ExperimentalSerializationApi
fun buildApiService(): RemoteApiService =
    buildRetrofit().create(RemoteApiService::class.java)