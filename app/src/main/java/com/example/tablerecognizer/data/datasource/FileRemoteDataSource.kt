package com.example.tablerecognizer.data.datasource

import android.graphics.Bitmap
import android.os.Environment
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val uniqueTime = generateDateTimeString()

        val tempFile = bitmapToTempFile(photo, "image_${uniqueTime}", ".jpg", File(System.getProperty("java.io.tmpdir")))

        val requestBody = RequestBody.create(MediaType.parse("image/jpeg"), tempFile)
        val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

        val call = photoRecognizerApi.sendPhoto(filePart)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val csvContent = response.body()?.byteStream()
                    // Сохраняем csvContent в файл
                    val resFile = saveCsvToFile(csvContent, "recognized_image_${uniqueTime}.csv")
                    if (resFile==null){
                        message = "К сожалению произошла какая-то ошибка."
                        return
                    }

                    message = "Файл с таблицей сохранен по адресу ${resFile.absolutePath}"
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle error
            }
        })
    }
    private fun bitmapToTempFile(bitmap: Bitmap, prefix: String, suffix: String, directory: File): File {
        val imageFile = File.createTempFile(prefix, suffix, directory)

        imageFile.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
        }

        return imageFile
    }
    private fun getDownloadDirectoryPath(): String {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return downloadsDirectory.absolutePath
    }
    private fun createFileInDownloadDirectory(fileName: String): File {
        val downloadDirectoryPath = getDownloadDirectoryPath()
        val file = File(downloadDirectoryPath, fileName)
        return file
    }
    fun saveCsvToFile(csvContent: InputStream?, fileName: String) :File? {
        if (csvContent == null) {
            return null
        }

        val file = createFileInDownloadDirectory(fileName)
        val outputStream = FileOutputStream(file)

        try {
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (csvContent.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
        } finally {
            outputStream.close()
            csvContent.close()
        }
        return file
    }
    fun generateDateTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
}