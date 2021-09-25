package com.raywenderlich.android.taskie.networking

import com.raywenderlich.android.taskie.model.Task
import com.raywenderlich.android.taskie.model.UserProfile
import com.raywenderlich.android.taskie.model.request.AddTaskRequest
import com.raywenderlich.android.taskie.model.request.UserDataRequest
import com.raywenderlich.android.taskie.model.response.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*

interface RemoteApiService {

    //use clearTypes instead of ResponseBody and RequestBody, cuz we use Moshi

    //rest method using endpoint
    @POST("/api/register")
    //RequestBody describes data we can send to the server
    //ResponseBody - describe data we receive from server
    fun registerUser(@Body request: UserDataRequest): Call<RegisterResponse>

    @GET("/api/note")
    fun getNotes(@Header("Authorization") token: String): Call<GetTasksResponse>

    @POST("/api/login")
    fun loginUser(@Body request: UserDataRequest): Call<LoginResponse>

    @GET("/api/user/profile")
    fun getUserProfile(@Header("Authorization") token: String): Call<UserProfileResponse>

    //query use for search spicific elements on the server
    //for example - something by id query
    @POST("api/note/complete")
    fun completeTask(@Header("Authorization") token: String,
                     @Query("id") noteId: String): Call<CompleteNoteResponse>

    @POST("api/note")
    fun addTask(@Header("Authorization") token: String,
                @Body request: AddTaskRequest): Call<Task>
}