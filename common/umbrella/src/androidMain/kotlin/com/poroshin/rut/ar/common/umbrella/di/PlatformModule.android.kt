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
    single<Cicerone<FlowRouter>> { Cicerone.create(FlowRouter(null)) }
    single<FlowRouter> { get<Cicerone<FlowRouter>>().router }
    single<NavigatorHolder> { get<Cicerone<FlowRouter>>().getNavigatorHolder() }
    single<Navigator> { NavigatorImpl() }
}


