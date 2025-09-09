package com.poroshin.rut.ar.di

import com.poroshin.rut.ar.common.ar.data.di.arDataModule
import com.poroshin.rut.ar.common.ar.domain.di.arDomainModule
import com.poroshin.rut.ar.common.ar.presentation.di.arPresentationModule
import com.poroshin.rut.ar.common.pdp.data.di.pdpDataModule
import com.poroshin.rut.ar.common.pdp.domain.di.pdpDomainModule
import com.poroshin.rut.ar.common.pdp.presentation.di.pdpPresentationModule
import com.poroshin.rut.ar.common.plp.data.di.plpDataModule
import com.poroshin.rut.ar.common.plp.domain.di.plpDomainModule
import com.poroshin.rut.ar.common.plp.presentation.di.plpPresentationModule
import org.koin.core.module.Module

/**
 * Collect all feature modules to be included into Koin start.
 */
fun featureModules(): List<Module> = listOf(
    // PLP
    plpDataModule,
    plpDomainModule,
    plpPresentationModule,
    // PDP
    pdpDataModule,
    pdpDomainModule,
    pdpPresentationModule,
    // AR
    arDataModule,
    arDomainModule,
    arPresentationModule,
)


