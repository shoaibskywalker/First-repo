package com.test.loginfirebase.apiInterface

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiService {

    @POST("v1/completions")
    fun sendMessage(
        @Header("Authorization") token: String,
        @Body requestBody: HashMap<String, Any>
    ): Call<ResponseBody>

}