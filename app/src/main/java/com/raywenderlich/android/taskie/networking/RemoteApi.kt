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

import android.util.Log
import com.google.gson.Gson
import com.raywenderlich.android.taskie.App
import com.raywenderlich.android.taskie.model.Task
import com.raywenderlich.android.taskie.model.UserProfile
import com.raywenderlich.android.taskie.model.request.AddTaskRequest
import com.raywenderlich.android.taskie.model.request.UserDataRequest
import com.raywenderlich.android.taskie.model.response.GetTasksResponse
import com.raywenderlich.android.taskie.model.response.LoginResponse
import com.raywenderlich.android.taskie.model.response.UserProfileResponse
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.NullPointerException
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

/**
 * Holds decoupled logic for all the API calls.
 */

const val BASE_URL = "https://taskie-rw.herokuapp.com"

//RemoteApi will be middleman between user interface and actual Api service
class RemoteApi(private val apiService: RemoteApiService) {

    private val gson = Gson()

  fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (String?, Throwable?) -> Unit) {
      val body = RequestBody.create(
          MediaType.parse("application/json"),
          gson.toJson(userDataRequest)
      )

      apiService.loginUser(body).enqueue(object : Callback<ResponseBody>{
          override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
              val jsonBody = response.body()?.string()
              if (jsonBody == null) {
                  onUserLoggedIn(null, NullPointerException("No response body"))
                  return
              }

              val loginResponse = gson.fromJson(jsonBody, LoginResponse::class.java)
              if (loginResponse == null || loginResponse.token.isNullOrBlank()) {
                  onUserLoggedIn(null, NullPointerException("No response body"))
              } else {
                  onUserLoggedIn(loginResponse.token, null)
              }
          }

          override fun onFailure(call: Call<ResponseBody>, error: Throwable) {
              onUserLoggedIn(null, error)
          }
      })
  }

  fun registerUser(userDataRequest: UserDataRequest, onUserCreated: (String?, Throwable?) -> Unit) {
      val body = RequestBody.create(
          //prepare data as a Request body
          MediaType.parse("application/json"), gson.toJson(userDataRequest)
      )

      //enqueue api call in background
      apiService.registerUser(body).enqueue(object : Callback<ResponseBody>{
          override fun onFailure(call: Call<ResponseBody>, error: Throwable) {
              //problems can be: internet connection, endpoint which doesnt exist or timing out
              onUserCreated(null, error)
          }
          override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
              //if there is any response from the server
              //it can be error too: you are unauthorized or server has a error
              val message = response.body()?.string()
              if (message == null) {
                  onUserCreated(null, NullPointerException("No response body!"))
                  return
              }
              val jsonObject = JSONObject(message.toString())
              onUserCreated(jsonObject.getString("message"), null)
          }
      })
  }

  fun getTasks(onTasksReceived: (List<Task>, Throwable?) -> Unit) {
      apiService.getNotes(App.getToken()).enqueue(object : Callback<ResponseBody> {

          override fun onFailure(call: Call<ResponseBody>, error: Throwable) {
              onTasksReceived(emptyList(), error)
          }

          override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
              val jsonBody = response.body()?.string()

              //check the response body
              if (jsonBody == null) {
                  onTasksReceived(emptyList(), NullPointerException("No data available"))
                  return
              }

              val data = gson.fromJson(jsonBody, GetTasksResponse::class.java)

              if (data != null && data.notes.isNotEmpty()) {
                  onTasksReceived(data.notes.filter { !it.isCompleted }, null)
              } else {
                  onTasksReceived(emptyList(), NullPointerException("No data available"))
              }
          }
      })
  }

  fun deleteTask(onTaskDeleted: (Throwable?) -> Unit) {
    onTaskDeleted(null)
  }

  fun completeTask(taskId: String, onTaskCompleted: (Throwable?) -> Unit) {
      Thread(Runnable {
          //?id=$taskId - this called is query; complete task by id
          //query with multiple parametres looks like this: complete?id=$taskId&title=$noteTitle
          val connection = URL("$BASE_URL/api/note/complete?id=$taskId").openConnection() as HttpURLConnection
          connection.requestMethod = "POST"
          connection.setRequestProperty("Content-Type", "application/json")
          connection.setRequestProperty("Accept", "application/json")
          connection.setRequestProperty("Authorization", App.getToken())
          connection.readTimeout = 10000
          connection.connectTimeout = 10000
          connection.doInput = true

          try {
              val reader = InputStreamReader(connection.inputStream)
              reader.use { input ->
                  val response = StringBuilder()
                  val bufferedReader = BufferedReader(input)

                  bufferedReader.useLines { lines ->
                      lines.forEach {
                          response.append(it.trim())
                      }
                  }
                  //i wanted to check te message, but we can do it without it
//                  val jsonObject = JSONObject(response.toString())
//                  if (jsonObject.getString("message").equals("Success")){
//                      onTaskCompleted(null)
//                  }
                  onTaskCompleted(null)
              }
          } catch (error: Throwable) {
              onTaskCompleted(error)
          }
          connection.disconnect()
      }).start()

  }

  fun addTask(addTaskRequest: AddTaskRequest, onTaskCreated: (Task?, Throwable?) -> Unit) {
      Thread(Runnable {
          val connection = URL("$BASE_URL/api/note").openConnection() as HttpURLConnection
          connection.requestMethod = "POST"
          connection.setRequestProperty("Content-Type", "application/json")
          connection.setRequestProperty("Accept", "application/json")
          connection.setRequestProperty("Authorization", App.getToken())
          connection.readTimeout = 10000
          connection.connectTimeout = 10000
          connection.doInput = true
          connection.doOutput = true

          //use gson to parse data to Json - easiest way
          val body = gson.toJson(addTaskRequest)

          try {
              connection.outputStream.use {
                  it.write(body.toByteArray())
              }
              val reader = InputStreamReader(connection.inputStream)
              reader.use { input ->
                  val response = StringBuilder()

                  val bufferedReader = BufferedReader(input)
                  bufferedReader.useLines { lines ->
                      lines.forEach {
                          response.append(it.trim())
                      }
                  }

                  val jsonObject = JSONObject(response.toString())

                  //create object from Json properties by keys
                  val task = Task(
                      jsonObject.getString("id"),
                      jsonObject.getString("title"),
                      jsonObject.getString("content"),
                      jsonObject.getBoolean("isCompleted"),
                      jsonObject.getInt("taskPriority")
                  )

                  onTaskCreated(task, null)
              }

          } catch (error: Throwable){
              Log.d("proverka", "error")
              onTaskCreated(null, error)
          }
          connection.disconnect()
      }).start()


  }

  fun getUserProfile(onUserProfileReceived: (UserProfile?, Throwable?) -> Unit) {
      getTasks { tasks, error ->
          //if the error is NP - means we passed it in getTasks earlier, cuz there is not list
          //we will just show empty list of tasks
          // if error is any Throwable except of NP - close the block
          if (error != null && error !is NullPointerException) {
              onUserProfileReceived(null, error)
              return@getTasks
          }

          //use nested Api call
          apiService.getUserProfile(App.getToken()).enqueue(object : Callback<ResponseBody>{

              override fun onFailure(call: Call<ResponseBody>, error: Throwable) {
                  onUserProfileReceived(null, error)
              }

              override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                  val jsonBody = response.body()?.string()

                  if (jsonBody == null) {
                      onUserProfileReceived(null, error)
                      return
                  }

                  val data = gson.fromJson(jsonBody, UserProfileResponse::class.java)

                  if (data.email == null || data.name == null){
                      onUserProfileReceived(null, error)
                  } else {
                      onUserProfileReceived(UserProfile(
                          data.email,
                          data.name,
                          tasks.size
                      ), null)
                  }
              }
          })
      }
  }
}