package com.example.tablerecognizer.domain.useCases

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.example.tablerecognizer.data.repositories.FileAndMessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FileAndMessageUseCase @Inject constructor(
    private val fileAndMessageRepository: FileAndMessageRepository,
) {
    val message: LiveData<String> = fileAndMessageRepository.message
    suspend fun sendPhoto(photo: Bitmap) {
        withContext(Dispatchers.Main) {
            fileAndMessageRepository.sendPhoto(photo)
        }
    }
}