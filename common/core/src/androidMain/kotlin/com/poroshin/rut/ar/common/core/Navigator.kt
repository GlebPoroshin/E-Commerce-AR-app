package com.poroshin.rut.ar.common.core

import android.os.Bundle
import com.github.terrakok.cicerone.Router

/**
 * Navigation abstraction for application modules.
 *
 * Implementation translates logical [NavigationTree] + parameters into specific Screen's and
 * executes commands on the passed [router].
 */
interface Navigator {
    /**
     * Execute navigation (push) to screen corresponding to [key].
     *
     * @param router Router on which to execute the command.
     * @param key logical screen key from [NavigationTree].
     * @param params additional parameters, empty [Bundle] by default.
     */
    fun navigateTo(router: Router, key: NavigationTree, params: Bundle = Bundle())

    /**
     * Start flow — semantically start new navigation flow (if router is [FlowRouter],
     * `startFlow` will be used, otherwise — regular `navigateTo`).
     *
     * @param router Router on which to execute the command.
     * @param key logical screen key from [NavigationTree].
     * @param params additional parameters, empty [Bundle] by default.
     */
    fun startFlow(router: Router, key: NavigationTree, params: Bundle = Bundle())

    /**
     * Set new navigation root (newRootScreen).
     *
     * @param router Router on which to execute the command.
     * @param key logical screen key from [NavigationTree].
     * @param params additional parameters, empty [Bundle] by default.
     */
    fun newRootScreen(router: Router, key: NavigationTree, params: Bundle = Bundle())

    /**
     * Execute "pop" — step back.
     *
     * @param router Router on which to execute the command.
     */
    fun pop(router: Router)

    /**
     * Execute pop to specified logical key (popTo).
     *
     * @param router Router on which to execute the command.
     * @param key logical screen key from [NavigationTree] to rollback to.
     * @param params additional parameters if Screen matching is needed (empty Bundle by default).
     */
    fun popTo(router: Router, key: NavigationTree, params: Bundle = Bundle())

    /**
     * Execute pop to navigation root.
     *
     * @param router Router on which to execute the command.
     */
    fun popToRoot(router: Router)
}
