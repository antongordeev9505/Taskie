package com.raywenderlich.android.taskie.networking

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*

interface RemoteApiService {

    //rest method using endpoint
    @POST("/api/register")
    //RequestBody describes data we can send to the server
    //ResponseBody - describe data we receive from server
    fun registerUser(@Body request: RequestBody): Call<ResponseBody>

    @GET("/api/note")
    fun getNotes(@Header("Authorization") token: String): Call<ResponseBody>

    @POST("/api/login")
    fun loginUser(@Body request: RequestBody): Call<ResponseBody>

    @GET("/api/user/profile")
    fun getUserProfile(@Header("Authorization") token: String): Call<ResponseBody>

    //query use for search spicific elements on the server
    //for example - something by id query
    @POST("api/note/complete")
    fun completeTask(@Header("Authorization") token: String,
                     @Query("id") noteId: String): Call<ResponseBody>

    @POST("api/note")
    fun addTask(@Header("Authorization") token: String,
                @Body request: RequestBody): Call<ResponseBody>



}