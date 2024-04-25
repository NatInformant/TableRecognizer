package com.example.tablerecognizer.data.repositories

import com.example.tablerecognizer.data.datasource.FileRemoteDataSource
import com.example.tablerecognizer.ioc.AppComponentScope
import javax.inject.Inject

@AppComponentScope
class FileAndMessageRepository @Inject constructor(
    val dataSource: FileRemoteDataSource
){

}