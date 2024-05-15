package com.example.tablerecognizer.data.datasource

import android.graphics.Bitmap
import android.os.Environment
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tablerecognizer.data.models.OcrRequest
import okhttp3.OkHttpClient
import java.io.IOException
import org.json.JSONObject
import java.lang.Exception

class FileRemoteDataSource {
    private val BASE_URL = "https://ocr.api.cloud.yandex.net/"
    private val photoRecognizerApi: PhotoRecognizerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PhotoRecognizerApi::class.java)
    }

    private val _message = MutableLiveData<String>("")
    val message: LiveData<String> = _message
    fun sendPhoto(photo: Bitmap) {
        val uniqueTime = generateDateTimeString()

        val tempFile = bitmapToTempFile(
            photo,
            "image_${uniqueTime}",
            ".jpg",
            File(System.getProperty("java.io.tmpdir")!!)
        )
        val content = Base64.encodeToString(tempFile.readBytes(), Base64.DEFAULT)

        val request = OcrRequest(
            mimeType = "JPEG",
            languageCodes = listOf("ru", "en"),
            model = "table",
            content = content
        )

        val call = photoRecognizerApi.recognizeText(request)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseBody = response.body()
                val resultPath =
                    saveCsvToFile(responseBody?.string(), "recognized_image_${uniqueTime}.json")
                if(resultPath==null){
                    _message.value = "There are no tables in your photo."
                }else{
                    _message.value = "Your files are saved at: $resultPath"
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _message.value =
                    "Unfortunately, the recognition did not work, take a clearer photo or check the connection."
            }
        })
    }

    private fun bitmapToTempFile(
        bitmap: Bitmap,
        prefix: String,
        suffix: String,
        directory: File
    ): File {
        val imageFile = File.createTempFile(prefix, suffix, directory)

        imageFile.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
        }

        return imageFile
    }

    private fun getDownloadDirectoryPath(): String {
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return downloadsDirectory.absolutePath
    }

    private fun createFileInDownloadDirectory(fileName: String): File {
        val downloadDirectoryPath = getDownloadDirectoryPath()
        return File(downloadDirectoryPath, fileName)
    }

    fun saveCsvToFile(csvContent: String?, fileName: String): String? {
        if (csvContent == null) {
            return null
        }

        val file = createFileInDownloadDirectory(fileName)
        return try {
            toCSV(csvContent, file.absolutePath)
        } catch (ex: Exception) {
            null
        }
    }

    private fun toCSV(content: String, jsonPath: String): String {
        var jsonFile: File? = null

        try {
            try {
                jsonFile = File(jsonPath)
                jsonFile.createNewFile()
                FileOutputStream(jsonFile).use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val obj = JSONObject(content)
            val result = obj.getJSONObject("result")
            val textAnnotation = result.getJSONObject("textAnnotation")
            val tables = textAnnotation.getJSONArray("tables")

            val table1 = tables.getJSONObject(0)
            val rowCount = table1.getInt("rowCount")
            val columnCount = table1.getInt("columnCount")
            val cells = table1.getJSONArray("cells")

            val sb = StringBuilder()
            for (i in 0 until rowCount) {
                for (j in 0 until columnCount) {
                    val index = i * columnCount + j
                    val cell = cells.getJSONObject(index)
                    val text = cell.getString("text")
                    sb.append(text)

                    if (j != columnCount - 1) {
                        sb.append(",")
                    } else {
                        sb.append("\n")
                    }
                }
            }

            val csvPath = jsonPath.replace(".json", ".csv")

            try {
                val file = File(csvPath)
                file.createNewFile()
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(sb.toString().toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            jsonFile!!.delete()

            return csvPath
        } catch (ex: Exception) {
            jsonFile!!.delete()
            throw Exception()
        }
    }

    private fun generateDateTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
}