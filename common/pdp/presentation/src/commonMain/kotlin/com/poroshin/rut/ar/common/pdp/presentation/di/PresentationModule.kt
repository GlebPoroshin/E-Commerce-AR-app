package com.poroshin.rut.ar.common.pdp.presentation.di

import com.poroshin.rut.ar.common.pdp.presentation.PdpViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val pdpPresentationModule: Module = module {
    factory { PdpViewModel(get(), get()) }
}


