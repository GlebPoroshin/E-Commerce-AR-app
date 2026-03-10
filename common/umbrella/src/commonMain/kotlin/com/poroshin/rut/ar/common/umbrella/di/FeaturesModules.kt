package com.poroshin.rut.ar.common.umbrella.di

import com.poroshin.rut.ar.common.ar.data.di.arDataModule
import com.poroshin.rut.ar.common.ar.domain.di.arDomainModule
import com.poroshin.rut.ar.common.ar.presentation.di.arPresentationModule
import com.poroshin.rut.ar.common.cart.data.di.cartDataModule
import com.poroshin.rut.ar.common.cart.domain.di.cartDomainModule
import com.poroshin.rut.ar.common.cart.presentation.di.cartPresentationModule
import com.poroshin.rut.ar.common.pdp.data.di.pdpDataModule
import com.poroshin.rut.ar.common.pdp.presentation.di.pdpPresentationModule
import com.poroshin.rut.ar.common.plp.data.di.plpDataModule
import com.poroshin.rut.ar.common.plp.domain.di.plpDomainModule
import com.poroshin.rut.ar.common.plp.presentation.di.plpPresentationModule
import org.koin.core.module.Module

fun featureModules(): List<Module> = listOf(
    cartDataModule,
    cartDomainModule,
    cartPresentationModule,
    plpDataModule,
    plpDomainModule,
    plpPresentationModule,
    pdpDataModule,
    pdpPresentationModule,
    arDataModule,
    arDomainModule,
    arPresentationModule,
)
