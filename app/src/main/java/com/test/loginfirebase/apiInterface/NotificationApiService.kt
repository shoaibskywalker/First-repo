package com.test.loginfirebase.apiInterface

import com.test.loginfirebase.data.model.NotificationRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApiService {

    @Headers("Content-Type: application/json")
    @POST("projects/login-and-pass-firebase/messages:send")
    suspend fun sendNotification(
        @Header("Authorization") token: String,
        @Body notification: NotificationRequest
    ): Response<ResponseBody>

}