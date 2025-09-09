package com.poroshin.rut.ar.android

import android.app.Application
import com.poroshin.rut.ar.common.umbrella.di.androidPlatformModule
import com.poroshin.rut.ar.common.umbrella.di.umbrellaCommonModules
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin { modules(umbrellaCommonModules() + androidPlatformModule) }
    }
}


