/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.taskie.networking

import com.raywenderlich.android.taskie.model.*
import com.raywenderlich.android.taskie.model.request.AddTaskRequest
import com.raywenderlich.android.taskie.model.request.UserDataRequest
import com.raywenderlich.android.taskie.model.response.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.NullPointerException

/**
 * Holds decoupled logic for all the API calls.
 */

const val BASE_URL = "https://taskie-rw.herokuapp.com"

//RemoteApi will be middleman between user interface and actual Api service
class RemoteApi(private val apiService: RemoteApiService) {

    //change types of object to ClearType
    //changed lambda - take Result type of the String, result can be success or failure
  fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (Result<String>) -> Unit) {

      //delete request body and all the code which convert Json/Kotlin, cuz we convert data using Moshi
      //send models directly and Moshi will convert it like object userDataRequest
      apiService.loginUser(userDataRequest).enqueue(object : Callback<LoginResponse>{
          override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
              val loginResponse = response.body()

              if (loginResponse == null || loginResponse.token.isNullOrBlank()) {
                  onUserLoggedIn(Failure(NullPointerException("No response body")))
              } else {
                  onUserLoggedIn(Success(loginResponse.token))
              }
          }

          override fun onFailure(call: Call<LoginResponse>, error: Throwable) {
              onUserLoggedIn(Failure(error))
          }
      })
  }

  fun registerUser(userDataRequest: UserDataRequest, onUserCreated: (Result<String>) -> Unit) {
      //enqueue api call in background
      apiService.registerUser(userDataRequest).enqueue(object : Callback<RegisterResponse>{
          override fun onFailure(call: Call<RegisterResponse>, error: Throwable) {
              //problems can be: internet connection, endpoint which doesnt exist or timing out
              onUserCreated(Failure(error))
          }
          override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
              //if there is any response from the server
              //it can be error too: you are unauthorized or server has a error
              val message = response.body()?.message
              if (message == null) {
                  onUserCreated(Failure(NullPointerException("No response body!")))
                  return
              }

              onUserCreated(Success(message))
          }
      })
  }

  suspend fun getTasks(): Result<List<Task>> = try {
      val data = apiService.getNotes()

      Success(data.notes.filter { !it.isCompleted })
  } catch (error: Throwable) {
      Failure(error)
  }

    //function return the result instead of send it through callback
    //using suspend modifiar we can use suspend functions inside function
    //deleteTask - suspend function - it means Main thread will not be blocked
    //executing request in try/catch block in background
    //return try/catch block
    //retrofit take care of Threading
    suspend fun deleteTask(taskId: String): Result<String> = try {
        val data = apiService.deleteTask(taskId)

        Success(data.message!!)
    } catch (error: Throwable) {
        Failure(error)
    }

  suspend fun completeTask(taskId: String): Result<String> = try {
      val data = apiService.completeTask(taskId)

      Success(data.message!!)
  } catch (error: Throwable) {
      Failure(error)
  }

  suspend fun addTask(addTaskRequest: AddTaskRequest): Result<Task> = try {
      val data = apiService.addTask(addTaskRequest)

      Success(data)
  } catch (error: Throwable) {
      Failure(error)
  }

  suspend fun getUserProfile(): Result<UserProfile> = try {
      val tasksResult = getTasks()

      if (tasksResult is Failure) {
          Failure(tasksResult.error)
      } else {
          val tasks = tasksResult as Success
          val data = apiService.getUserProfile()

          if (data.email == null ||data.name == null){
              Failure(NullPointerException("No data available"))
          } else {
              Success(UserProfile(data.email, data.name, tasks.data.size))
          }
      }
  } catch (error: Throwable) {
      Failure(error)
  }

}