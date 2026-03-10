package com.poroshin.rut.ar.common.cart.data.di

import com.poroshin.rut.ar.common.cart.data.db.CartDatabase
import com.poroshin.rut.ar.common.cart.data.db.DatabaseDriverFactory
import com.poroshin.rut.ar.common.cart.data.repository.CartRepositoryImpl
import com.poroshin.rut.ar.common.cart.data.usecase.AddOrIncrementCartItemUseCaseImpl
import com.poroshin.rut.ar.common.cart.data.usecase.DecrementCartItemUseCaseImpl
import com.poroshin.rut.ar.common.cart.data.usecase.ObserveCartBadgeCountUseCaseImpl
import com.poroshin.rut.ar.common.cart.data.usecase.ObserveCartLinesUseCaseImpl
import com.poroshin.rut.ar.common.cart.data.usecase.ObserveCartSummaryUseCaseImpl
import com.poroshin.rut.ar.common.cart.data.usecase.ObserveSkuQuantityUseCaseImpl
import com.poroshin.rut.ar.common.cart.data.usecase.RemoveCartItemUseCaseImpl
import com.poroshin.rut.ar.common.cart.data.usecase.RunLegacyModelVersionsMigrationUseCaseImpl
import com.poroshin.rut.ar.common.cart.data.usecase.SetCartItemQuantityUseCaseImpl
import com.poroshin.rut.ar.common.cart.domain.repository.CartRepository
import com.poroshin.rut.ar.common.cart.domain.usecase.AddOrIncrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.DecrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartBadgeCountUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartLinesUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartSummaryUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveSkuQuantityUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.RemoveCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.RunLegacyModelVersionsMigrationUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.SetCartItemQuantityUseCase
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

val cartDataModule: Module = module {
    single { Settings() }
    single { DatabaseDriverFactory() }
    single { CartDatabase(driver = get<DatabaseDriverFactory>().createDriver()) }

    single<CartRepository> { CartRepositoryImpl(get(), get()) }

    single<ObserveCartLinesUseCase> { ObserveCartLinesUseCaseImpl(get()) }
    single<ObserveCartBadgeCountUseCase> { ObserveCartBadgeCountUseCaseImpl(get()) }
    single<ObserveSkuQuantityUseCase> { ObserveSkuQuantityUseCaseImpl(get()) }
    single<ObserveCartSummaryUseCase> { ObserveCartSummaryUseCaseImpl(get()) }

    single<AddOrIncrementCartItemUseCase> { AddOrIncrementCartItemUseCaseImpl(get()) }
    single<DecrementCartItemUseCase> { DecrementCartItemUseCaseImpl(get()) }
    single<SetCartItemQuantityUseCase> { SetCartItemQuantityUseCaseImpl(get()) }
    single<RemoveCartItemUseCase> { RemoveCartItemUseCaseImpl(get()) }

    single<RunLegacyModelVersionsMigrationUseCase> { RunLegacyModelVersionsMigrationUseCaseImpl(get()) }
}
