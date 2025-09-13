package com.poroshin.rut.ar.android

import android.app.Application
import android.content.Context
import com.poroshin.rut.ar.common.pdp.data.pdpDataAndroidModule
import com.poroshin.rut.ar.common.umbrella.di.androidPlatformModule
import com.poroshin.rut.ar.common.umbrella.di.umbrellaCommonModules
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(umbrellaCommonModules()
                    + androidPlatformModule
                    + pdpDataAndroidModule
                    + module { single<Context> { this@App } }
            )
        }
    }
}
