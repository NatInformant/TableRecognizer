package com.example.tablerecognizer.data.datasource

import android.graphics.Bitmap
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FileRemoteDataSource {
    private val BASE_URL = "https://smiling-striking-lionfish.ngrok-free.app/api/"
    private val photoRecognizerApi: PhotoRecognizerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PhotoRecognizerApi::class.java)
    }

    private var message: String = ""
    fun loadMessage() = message
    suspend fun sendPhoto(photo: Bitmap) {
        /*val response = photoRecognizerApi.getFile(
            photo
        )
        if (response.isSuccessful) {
            //Ждём бек
        }*/
        message = "Файл с таблице сохранен по адресу C:\\ProgramData\\Adobe"
    }
}