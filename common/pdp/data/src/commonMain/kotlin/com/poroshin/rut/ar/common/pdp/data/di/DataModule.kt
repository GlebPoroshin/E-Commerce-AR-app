package com.poroshin.rut.ar.common.pdp.data.di

import com.poroshin.rut.ar.common.pdp.data.GetProductPageInfoUseCaseImpl
import com.poroshin.rut.ar.common.pdp.domain.GetProductPageInfoUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val pdpDataModule: Module = module {
    single<GetProductPageInfoUseCase> { GetProductPageInfoUseCaseImpl() }
}


