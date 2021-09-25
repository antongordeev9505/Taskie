package com.raywenderlich.android.taskie.model

//more friendly way to handling errors
//sealed class can have only fixed set of subtypes: Success and Failure
sealed class Result<out T : Any>
//get data property of someType(any)
data class Success<out T : Any>(val data: T) : Result<T>()

data class Failure(val error: Throwable) : Result<Nothing>()


