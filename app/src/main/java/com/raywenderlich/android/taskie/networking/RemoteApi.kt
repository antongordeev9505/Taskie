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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

/**
 * Holds decoupled logic for all the API calls.
 */

const val BASE_URL = "https://taskie-rw.herokuapp.com"

class RemoteApi {

    private val gson = Gson()

  fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (String?, Throwable?) -> Unit) {
      Thread(Runnable {
          //another endpoint for login
          val connection = URL("$BASE_URL/api/login").openConnection() as HttpURLConnection
          connection.requestMethod = "POST"

          connection.setRequestProperty("Content-Type", "application/json")
          connection.setRequestProperty("Accept", "application/json")
          connection.readTimeout = 10000
          connection.connectTimeout = 10000
          connection.doInput = true
          connection.doOutput = true


          //use gson to parse data to Json - easiest way
          val body = gson.toJson(userDataRequest)

          val bytes = body.toByteArray()

          try {
              connection.outputStream.use {
                  it.write(bytes)
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
                    //parse the response
                  val jsonObject = JSONObject(response.toString())
                  val token = jsonObject.getString("token")

                  onUserLoggedIn(token, null)
              }

          } catch (error: Throwable){
              onUserLoggedIn(null, error)
          }

          connection.disconnect()
      }).start()
  }

  fun registerUser(userDataRequest: UserDataRequest, onUserCreated: (String?, Throwable?) -> Unit) {
    //in background
      Thread(Runnable {
          //open HTTP connection to specific URL
          //text after BASEURL - end point - say what functionality we get - in this case register user
          val connection = URL("$BASE_URL/api/register").openConnection() as HttpURLConnection
          //sending data to server - POST
          connection.requestMethod = "POST"
          //JSON format for data
          //A header saying in which format the communication to the server should be
          connection.setRequestProperty("Content-Type", "application/json")
          //A header saying in which format the response from the server will be
          connection.setRequestProperty("Accept", "application/json")
          connection.readTimeout = 10000
          connection.connectTimeout = 10000
          //can use output and input data
          connection.doInput = true
          connection.doOutput = true

          //use gson to parse data to Json - easiest way
          val body = gson.toJson(userDataRequest)

          val bytes = body.toByteArray()

          try {
              //write bytes to output Stream
              //use - automaticly close the stream when it will be done
              connection.outputStream.use {
                  it.write(bytes)
              }
                //read response from inputStream
              val reader = InputStreamReader(connection.inputStream)
              reader.use { input ->
                  //response - variable for response from server
                  val response = StringBuilder()
                  //use BR cuz read one chunk at the time - better way to read
                  val bufferedReader = BufferedReader(input)

                  bufferedReader.useLines { lines ->
                      lines.forEach {
                          response.append(it.trim())
                      }
                  }

                  val jsonObject = JSONObject(response.toString())

                  //finally send back response
                  onUserCreated(jsonObject.getString("message"), null)
              }

          } catch (error: Throwable){
              //if error
              onUserCreated(null, error)
          }
          connection.disconnect()
      }).start()
  }

  fun getTasks(onTasksReceived: (List<Task>, Throwable?) -> Unit) {
      Thread(Runnable {
          val connection = URL("$BASE_URL/api/note").openConnection() as HttpURLConnection
          connection.requestMethod = "GET"
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
                  //parse json object to Kotlin Object GetTasksResponse
                  val taskResponse = gson.fromJson(response.toString(), GetTasksResponse::class.java)
                  onTasksReceived(taskResponse?.notes ?: listOf(), null)
              }
          } catch (error: Throwable) {
              onTasksReceived(emptyList(), error)
          }
          connection.disconnect()
      }).start()
  }

  fun deleteTask(onTaskDeleted: (Throwable?) -> Unit) {
    onTaskDeleted(null)
  }

  fun completeTask(onTaskCompleted: (Throwable?) -> Unit) {
    onTaskCompleted(null)
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
    onUserProfileReceived(UserProfile("mail@mail.com", "Filip", 10), null)
  }
}