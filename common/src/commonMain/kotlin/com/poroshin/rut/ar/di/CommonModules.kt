package com.poroshin.rut.ar.di

import org.koin.core.module.Module

/**
 * Aggregates all common DI modules. Extend this when wiring feature modules.
 */
fun commonModules(): List<Module> = listOf(
    coreModule,
) + featureModules()


