package com.example.tablerecognizer.ioc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tablerecognizer.domain.useCases.FileAndMessageUseCase
import com.example.tablerecognizer.ui.view.MainViewModel
import javax.inject.Inject

class MainViewModelFactory @Inject constructor(
    val fileAndMessageUseCase: FileAndMessageUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            fileAndMessageUseCase = fileAndMessageUseCase,
        ) as T
    }
}