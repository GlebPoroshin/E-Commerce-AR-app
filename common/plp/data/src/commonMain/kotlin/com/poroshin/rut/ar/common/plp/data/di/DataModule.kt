package com.poroshin.rut.ar.common.plp.data.di

import com.poroshin.rut.ar.common.plp.data.GetPlpProductsUseCaseImpl
import org.koin.core.module.Module
import org.koin.dsl.module
import com.poroshin.rut.ar.common.plp.domain.GetPlpProductsUseCase

val plpDataModule: Module = module {
    single<GetPlpProductsUseCase> { GetPlpProductsUseCaseImpl() }
}


