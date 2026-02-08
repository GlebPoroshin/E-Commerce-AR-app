package com.poroshin.rut.ar.common.umbrella.di

import com.poroshin.rut.ar.common.cart.domain.usecase.RunLegacyModelVersionsMigrationUseCase
import com.poroshin.rut.ar.common.pdp.data.pdpDataIOSModule
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

private var koinApplication: KoinApplication? = null

fun initKoinIos(additionalModules: List<Module> = emptyList()) {
    koinApplication = startKoin {
        modules(umbrellaCommonModules() + listOf(iosPlatformModule) + additionalModules)
    }
}

fun doInitKoin() {
    initKoinIos(listOf(pdpDataIOSModule))
    runBlocking {
        koinApplication?.koin?.get<RunLegacyModelVersionsMigrationUseCase>()?.invoke()
    }
}
