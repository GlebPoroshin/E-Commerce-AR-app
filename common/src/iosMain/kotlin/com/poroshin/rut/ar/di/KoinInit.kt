package com.poroshin.rut.ar.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Initializes Koin for iOS. Call from Swift early in app lifecycle.
 */
fun initKoinIos(additionalModules: List<Module> = emptyList()) {
    startKoin {
        modules(commonModules() + listOf(iosPlatformModule) + additionalModules)
    }
}


