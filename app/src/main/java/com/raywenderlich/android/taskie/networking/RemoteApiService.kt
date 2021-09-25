package com.raywenderlich.android.taskie.networking

import com.raywenderlich.android.taskie.model.Task
import com.raywenderlich.android.taskie.model.request.AddTaskRequest
import com.raywenderlich.android.taskie.model.request.UserDataRequest
import com.raywenderlich.android.taskie.model.response.*
import retrofit2.Call
import retrofit2.http.*

interface RemoteApiService {

    //use clearTypes instead of ResponseBody and RequestBody, cuz we use Moshi

    //rest method using endpoint
    @POST("/api/register")
    //RequestBody describes data we can send to the server
    //ResponseBody - describe data we receive from server
    fun registerUser(@Body request: UserDataRequest): Call<RegisterResponse>

    @GET("/api/note")
    suspend fun getNotes(): GetTasksResponse

    @POST("/api/login")
    fun loginUser(@Body request: UserDataRequest): Call<LoginResponse>

    @GET("/api/user/profile")
    suspend fun getUserProfile(): UserProfileResponse

    //query use for search spicific elements on the server
    //for example - something by id query
    @POST("api/note/complete")
    suspend fun completeTask(@Query("id") noteId: String): CompleteNoteResponse

    @POST("api/note")
    suspend fun addTask(@Body request: AddTaskRequest): Task

    //cuz we changed to suspend - retrofit understood it and generated code for threading
    //also return only model without Call
    @DELETE("api/note")
    suspend fun deleteTask(@Query("id") noteId: String): DeleteNoteResponse

}