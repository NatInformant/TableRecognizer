package com.example.tablerecognizer.data.repositories

import android.graphics.Bitmap
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tablerecognizer.data.datasource.FileRemoteDataSource
import com.example.tablerecognizer.ioc.AppComponentScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AppComponentScope
class FileAndMessageRepository @Inject constructor(
    val dataSource: FileRemoteDataSource
){
    private val _message = MutableLiveData<String>("")
    val message: LiveData<String> = _message
    @MainThread
    suspend fun sendPhoto(photo: Bitmap) {
        withContext(Dispatchers.IO) {
            dataSource.sendPhoto(photo)
        }
    }
}