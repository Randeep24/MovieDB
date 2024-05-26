package com.randeep.moviedb.data.remote.networkUtil

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

inline fun <T> safeApiCall(apiCall: () -> T): RemoteResult<T> {
        return try {
                RemoteResult.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
                when(throwable) {
                        is SocketTimeoutException -> {
                                RemoteResult.Error(TimeOut(throwable))
                        }
                        is UnknownHostException -> {
                                RemoteResult.Error(NoInternet(throwable))
                        }
                        is IOException -> {
                                RemoteResult.Error(IOError(throwable))
                        }
                        is HttpException -> {
                                val body = throwable.response()?.errorBody()
                                val errorMessage = getHttpErrorMessage(body)
                                RemoteResult.Error(HTTPError(throwable = throwable, code = throwable.code(), httpErrorMessage = errorMessage))
                        }
                        else -> {
                                RemoteResult.Error(Other(throwable))
                        }
                }
        }
}

fun getHttpErrorMessage(responseBody: ResponseBody?): String? {
        return try {
                val jsonObject = JSONObject(responseBody!!.string())
                when {
                        jsonObject.has("error") -> jsonObject.getString("error")
                        jsonObject.has("message") -> jsonObject.getString("message")
                        jsonObject.has("Message") -> jsonObject.getString("Message")
                        else -> null
                }
        } catch (e: Exception) {
                null
        }
}