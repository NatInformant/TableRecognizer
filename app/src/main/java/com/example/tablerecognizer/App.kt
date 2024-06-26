package com.example.tablerecognizer

import android.app.Application
import android.location.Location
import com.example.tablerecognizer.ioc.ApplicationComponent
import com.example.tablerecognizer.ioc.DaggerApplicationComponent

class App : Application() {
    val applicationComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder().build()
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
    }

    companion object {
        private var sInstance: App? = null
        fun getInstance(): App {
            return requireNotNull(sInstance) { "I really don't how you get there." }
        }
    }
}
