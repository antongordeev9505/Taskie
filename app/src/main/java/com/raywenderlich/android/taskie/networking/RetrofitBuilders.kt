package com.raywenderlich.android.taskie.networking

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.raywenderlich.android.taskie.App
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

private const val HEADER_AUTHORIZATION = "Authorization"
//default client
fun buildClient(): OkHttpClient =
    OkHttpClient.Builder()
            //added interceptor, to intercept on BODY level
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(buildAuthorizationInterceptor())
        .build()

fun buildAuthorizationInterceptor() = object : Interceptor {
    //interceptor receive interceptor chain
    //chain - all the layers APICall goes through before returning response
    override fun intercept(chain: Interceptor.Chain): Response {

        //get original request
        val originalRequest = chain.request()
        //check existence of token, if there is not - proceed with original request
        if (App.getToken().isBlank()) return chain.proceed(originalRequest)

        //if there is token - create new request with header
        val new = originalRequest.newBuilder()
            .addHeader(HEADER_AUTHORIZATION, App.getToken())
            .build()
        //this will be done with all requests - means we will be authorized for all calls

        return chain.proceed(new)
    }
}

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