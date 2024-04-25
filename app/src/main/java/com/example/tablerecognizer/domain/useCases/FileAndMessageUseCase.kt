package com.example.tablerecognizer.domain.useCases

import com.example.tablerecognizer.data.repositories.FileAndMessageRepository
import javax.inject.Inject

class FileAndMessageUseCase @Inject constructor(
    private val fileAndMessageRepository: FileAndMessageRepository,
) {

}