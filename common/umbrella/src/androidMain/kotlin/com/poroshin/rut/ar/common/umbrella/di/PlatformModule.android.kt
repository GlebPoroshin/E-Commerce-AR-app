package com.poroshin.rut.ar.common.umbrella.di

import org.koin.core.module.Module
import org.koin.dsl.module
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.poroshin.rut.ar.common.umbrella.navigation.FlowRouter
import com.poroshin.rut.ar.common.umbrella.navigation.Navigator
import com.poroshin.rut.ar.common.umbrella.navigation.NavigatorImpl

val androidPlatformModule: Module = module {
    val cicerone: Cicerone<Router> = Cicerone.create(FlowRouter(null))

    single<Navigator> { NavigatorImpl() }
    single<FlowRouter> { cicerone.router as FlowRouter }
}


