package com.poroshin.rut.ar.common.umbrella.di

import org.koin.core.module.Module

fun umbrellaCommonModules(): List<Module> = listOf(
    coreModule,
) + featureModules()


