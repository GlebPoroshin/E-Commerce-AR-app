package com.poroshin.rut.ar.common.umbrella.di

import com.poroshin.rut.ar.common.pdp.data.pdpDataIOSdModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoinIos(additionalModules: List<Module> = emptyList()) {
    startKoin {
        modules(umbrellaCommonModules() + listOf(iosPlatformModule) + additionalModules)
    }
}

fun doInitKoin() = initKoinIos(listOf(pdpDataIOSdModule))


