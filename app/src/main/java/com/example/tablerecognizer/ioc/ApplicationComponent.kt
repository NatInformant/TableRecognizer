package com.example.tablerecognizer.ioc

import com.example.tablerecognizer.data.datasource.FileRemoteDataSource
import com.example.tablerecognizer.domain.useCases.FileAndMessageUseCase
import dagger.Provides
import javax.inject.Scope

@Scope
annotation class AppComponentScope

@dagger.Component(modules = [DataModule::class, InteractiveMapViewModelModule::class])
@AppComponentScope
interface ApplicationComponent {
    fun getMainViewModelFactory(): ChatsViewModelFactory
}

@dagger.Module
object DataModule {
    @Provides
    @AppComponentScope
    fun getFileDataSource() = FileRemoteDataSource()
}
@dagger.Module
object InteractiveMapViewModelModule {
    @Provides
    @AppComponentScope
    fun getMainViewModelFactory(
        fileAndMessageUseCase: FileAndMessageUseCase,
    ): MainViewModelFactory {
        return MainViewModelFactory(fileAndMessageUseCase)
    }
}
