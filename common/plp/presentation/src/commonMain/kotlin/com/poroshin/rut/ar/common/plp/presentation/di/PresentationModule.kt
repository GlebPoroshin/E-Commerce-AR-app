package com.poroshin.rut.ar.common.plp.presentation.di

import com.poroshin.rut.ar.common.plp.presentation.PlpTestViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val plpPresentationModule: Module = module {
    factory { PlpTestViewModel() }
}


