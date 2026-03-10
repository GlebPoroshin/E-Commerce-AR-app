package com.poroshin.rut.ar.common.umbrella.navigation

import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen

/**
 * Router wrapper that redirects navigation commands to `parentRouter` if it's set.
 *
 * Used for organizing independent flows, where there's a parent Router (e.g., in Activity) and child FlowRouter's for modules.
 *
 * @param parentRouter if not null — all commands will be executed on it, otherwise — on this instance.
 */
class FlowRouter(private val parentRouter: Router?) : Router() {

    /**
     * Starts flow by navigating to specified screen.
     *
     * @param screen screen from which flow starts.
     */
    fun startFlow(screen: Screen) {
        runCommand { navigateTo(screen) }
    }

    /**
     * Replaces navigation root with passed screen — starting new flow root.
     *
     * @param screen screen that will become new root.
     */
    fun newRootFlow(screen: Screen) {
        runCommand { newRootScreen(screen) }
    }

    /**
     * Finishes current flow (calls `exit()`).
     */
    fun finishFlow() {
        runCommand { exit() }
    }

    /**
     * Executes "pop" — one step back (equivalent to Router.exit()).
     */
    fun pop() {
        runCommand { exit() }
    }

    /**
     * Executes pop to specified screen (backTo).
     *
     * @param screen screen to rollback to. If screen not found in stack — Router behavior.
     */
    fun popTo(screen: Screen) {
        runCommand { backTo(screen) }
    }

    /**
     * Executes pop to navigation root. Uses `backTo(null)` — rollback to deepest root.
     */
    fun popToRoot() {
        runCommand { backTo(null) }
    }

    /**
     * Executes navigation command — on `parentRouter` if it's set, otherwise on this instance.
     *
     * @param command lambda with Router.* calls (navigateTo, newRootScreen, exit etc.).
     */
    private fun runCommand(command: Router.() -> Unit) {
        if (parentRouter != null) parentRouter.command() else this.command()
    }
}
