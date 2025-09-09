package com.poroshin.rut.ar.common.umbrella.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoinIos(additionalModules: List<Module> = emptyList()) {
    startKoin {
        modules(umbrellaCommonModules() + listOf(iosPlatformModule) + additionalModules)
    }
}

fun doInitKoin() = initKoinIos()


