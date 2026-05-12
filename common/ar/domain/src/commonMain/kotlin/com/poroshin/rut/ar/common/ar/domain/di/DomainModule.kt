package com.poroshin.rut.ar.common.ar.domain.di

import com.poroshin.rut.ar.common.ar.domain.usecase.PlaceModelUseCase
import com.poroshin.rut.ar.common.ar.domain.usecase.RotateModelUseCase
import com.poroshin.rut.ar.common.ar.domain.usecase.ScaleModelUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val arDomainModule: Module = module {
    factory { RotateModelUseCase() }
    factory { ScaleModelUseCase() }
    factory { PlaceModelUseCase() }
}


