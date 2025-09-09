package com.poroshin.rut.ar.android

import android.app.Application
import com.poroshin.rut.ar.di.androidPlatformModule
import com.poroshin.rut.ar.di.commonModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(commonModules() + androidPlatformModule)
        }
    }
}


