package com.example.tablerecognizer.data.datasource

import android.graphics.Bitmap
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET

interface PhotoRecognizerApi {
    @GET("photo")
    suspend fun getFile(
        @Body requestBody: Bitmap,
    ): Response<GetResponse>
}