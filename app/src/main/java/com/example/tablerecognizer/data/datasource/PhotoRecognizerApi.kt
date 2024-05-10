package com.example.tablerecognizer.data.datasource

import com.example.tablerecognizer.data.models.OcrRequest
import com.example.tablerecognizer.data.models.OcrResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PhotoRecognizerApi {
    @Headers("Authorization: Api-Key AQVN06y8lyeF5FmHJCzqln9AG__qwzI9TcViY2MJ")
    @POST("ocr/v1/recognizeText")
    fun recognizeText(@Body request: OcrRequest): Call<ResponseBody>
}