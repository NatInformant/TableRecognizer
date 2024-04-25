package com.example.tablerecognizer.ui.view

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tablerecognizer.domain.useCases.FileAndMessageUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    val fileAndMessageUseCase: FileAndMessageUseCase,
) : ViewModel() {
    val message: LiveData<String> = fileAndMessageUseCase.message
    fun sendPhoto(photo:Bitmap){
        viewModelScope.launch {
            fileAndMessageUseCase.sendPhoto(photo)
        }
    }
}