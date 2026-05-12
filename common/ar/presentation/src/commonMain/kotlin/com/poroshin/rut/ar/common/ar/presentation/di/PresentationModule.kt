package com.poroshin.rut.ar.common.ar.presentation.di

import com.poroshin.rut.ar.common.ar.presentation.ArViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val arPresentationModule: Module = module {
    factory(
        qualifier = null,
        definition = { ArViewModel(get(), get(), get()) }
    )
}


