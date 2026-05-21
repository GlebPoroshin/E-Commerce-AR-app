package com.poroshin.rut.ar.common.umbrella.di

import com.poroshin.rut.ar.common.cart.domain.usecase.RunLegacyModelVersionsMigrationUseCase
import com.poroshin.rut.ar.common.core.BackendConfig
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

fun doInitKoin(useMockFallback: Boolean, arDemoBlackBg: Boolean = false) {
    BackendConfig.setUseMockFallback(useMockFallback)
    BackendConfig.setArDemoBlackBg(arDemoBlackBg)
    initKoinIos(listOf(pdpDataIOSModule))
    runBlocking {
        runCatching {
            koinApplication?.koin?.get<RunLegacyModelVersionsMigrationUseCase>()?.invoke()
        }.onFailure {
            println("Legacy model versions migration failed: ${it.message}")
        }
    }
}

fun doInitKoin() {
    doInitKoin(useMockFallback = false)
}

fun isArDemoBlackBg(): Boolean = BackendConfig.isArDemoBlackBg()
