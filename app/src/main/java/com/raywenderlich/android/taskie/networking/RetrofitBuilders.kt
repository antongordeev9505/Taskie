package com.raywenderlich.android.taskie.networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

//default client
fun buildClient(): OkHttpClient =
    OkHttpClient.Builder()
        .build()

//setup retrofit using client and baseURL
fun buildRetrofit(): Retrofit {
    return Retrofit.Builder()
        .client(buildClient())
            // use moshi converter: asLenient means shouldnt parse all data from Json
        .addConverterFactory(MoshiConverterFactory.create().asLenient())
        .baseUrl(BASE_URL)
        .build()
}

//build the API service
fun buildApiService(): RemoteApiService =
    buildRetrofit().create(RemoteApiService::class.java)