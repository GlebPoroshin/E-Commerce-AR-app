package com.poroshin.rut.ar.common.pdp.data.di

import com.poroshin.rut.ar.common.pdp.data.datasource.LocalModelDataSource
import com.poroshin.rut.ar.common.pdp.data.datasource.RemoteModelDataSource
import com.poroshin.rut.ar.common.pdp.data.repository.ModelRepositoryImpl
import com.poroshin.rut.ar.common.pdp.data.usecase.CheckModelExistsUseCaseImpl
import com.poroshin.rut.ar.common.pdp.data.usecase.DeleteProductModelUseCaseImpl
import com.poroshin.rut.ar.common.pdp.data.usecase.DownloadProductModelUseCaseImpl
import com.poroshin.rut.ar.common.pdp.data.usecase.GetProductPageInfoUseCaseImpl
import com.poroshin.rut.ar.common.pdp.domain.repository.ModelRepository
import com.poroshin.rut.ar.common.pdp.domain.usecase.CheckModelExistsUseCase
import com.poroshin.rut.ar.common.pdp.domain.usecase.DeleteProductModelUseCase
import com.poroshin.rut.ar.common.pdp.domain.usecase.DownloadProductModelUseCase
import com.poroshin.rut.ar.common.pdp.domain.usecase.GetProductPageInfoUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val pdpDataModule: Module = module {
    single { LocalModelDataSource(get()) }
    single { RemoteModelDataSource(get()) }

    single<ModelRepository> { ModelRepositoryImpl(get(), get()) }

    single<GetProductPageInfoUseCase> { GetProductPageInfoUseCaseImpl(get()) }
    single<DownloadProductModelUseCase> { DownloadProductModelUseCaseImpl(get()) }
    single<CheckModelExistsUseCase> { CheckModelExistsUseCaseImpl(get()) }
    single<DeleteProductModelUseCase> { DeleteProductModelUseCaseImpl(get()) }
}
