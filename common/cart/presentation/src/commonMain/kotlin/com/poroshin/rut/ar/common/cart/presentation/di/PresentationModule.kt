package com.poroshin.rut.ar.common.cart.presentation.di

import com.poroshin.rut.ar.common.cart.presentation.CartBadgeViewModel
import com.poroshin.rut.ar.common.cart.presentation.CartQuantityViewModel
import com.poroshin.rut.ar.common.cart.presentation.CartViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val cartPresentationModule: Module = module {
    factory { CartViewModel(get(), get(), get(), get(), get(), get()) }
    factory { CartBadgeViewModel(get()) }
    factory { CartQuantityViewModel(get(), get(), get()) }
}
