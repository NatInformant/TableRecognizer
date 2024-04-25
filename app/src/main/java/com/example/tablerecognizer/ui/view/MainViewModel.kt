package com.example.tablerecognizer.ui.view

import androidx.lifecycle.ViewModel
import com.example.tablerecognizer.domain.useCases.FileAndMessageUseCase
import javax.inject.Inject

class MainViewModel @Inject constructor(
    val fileAndMessageUseCase: FileAndMessageUseCase,
) : ViewModel() {

}