package com.example.tablerecognizer.data.datasource

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PhotoRecognizerApi {
    @Multipart
    @POST("photo")
    suspend fun sendPhoto(
        @Part file: MultipartBody.Part,
    ): Call<ResponseBody>
}