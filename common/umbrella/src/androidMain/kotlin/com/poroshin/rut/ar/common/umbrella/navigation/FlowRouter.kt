package com.poroshin.rut.ar.common.umbrella.navigation

import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen

class FlowRouter(private val parentRouter: Router?) : Router() {

    fun startFlow(screen: Screen) {
        runCommand { navigateTo(screen) }
    }

    fun newRootFlow(screen: Screen) {
        runCommand { newRootScreen(screen) }
    }

    fun finishFlow() {
        runCommand { exit() }
    }

    private fun runCommand(command: Router.() -> Unit) {
        if (parentRouter != null) parentRouter.command() else this.command()
    }
}


