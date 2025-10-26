package com.poroshin.rut.ar.common.umbrella.navigation

import android.os.Bundle
import com.github.terrakok.cicerone.Screen
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.poroshin.rut.ar.common.plp.presentation.PlpFragment
import com.poroshin.rut.ar.common.pdp.presentation.PdpFragment
import com.poroshin.rut.ar.common.ar.presentation.ARFragment
import com.poroshin.rut.ar.common.core.NavigationTree
import com.poroshin.rut.ar.common.core.Navigator

/**
 * Implementation of [Navigator] that converts [NavigationTree] to specific [Screen] (fragments).
 *
 * If in the future it becomes necessary to pass parameters to fragments — add them to `Bundle` inside
 * `getFragmentScreen` (in current implementation `params` are not used when creating fragments).
 */
class NavigatorImpl : Navigator {

    /**
     * Executes navigateTo with screen corresponding to [key].
     */
    override fun navigateTo(router: Router, key: NavigationTree, params: Bundle) {
        router.navigateTo(getFragmentScreen(key, params))
    }

    /**
     * If [router] is [FlowRouter], calls `startFlow`, otherwise — regular `navigateTo`.
     */
    override fun startFlow(router: Router, key: NavigationTree, params: Bundle) {
        (router as? FlowRouter)?.startFlow(getFragmentScreen(key, params))
            ?: router.navigateTo(getFragmentScreen(key, params))
    }

    /**
     * Replaces navigation root with screen corresponding to [key].
     */
    override fun newRootScreen(router: Router, key: NavigationTree, params: Bundle) {
        router.newRootScreen(getFragmentScreen(key, params))
    }

    /**
     * Execute "pop" — step back.
     */
    override fun pop(router: Router) {
        router.exit()
    }

    /**
     * Execute pop to screen corresponding to [key].
     */
    override fun popTo(router: Router, key: NavigationTree, params: Bundle) {
        router.backTo(getFragmentScreen(key, params))
    }

    /**
     * Execute pop to navigation root.
     */
    override fun popToRoot(router: Router) {
        router.backTo(null)
    }

    /**
     * Mapping [NavigationTree] -> [Screen].
     *
     * @param key navigation key.
     * @param params additional parameters (not applied in current implementation, but left for extension).
     * @return [Screen] with corresponding fragment.
     */
    private fun getFragmentScreen(key: NavigationTree, params: Bundle): Screen {
        return when (key) {
            NavigationTree.Plp -> FragmentScreen { PlpFragment.newInstance().apply { arguments = params } }
            NavigationTree.Pdp -> FragmentScreen { PdpFragment.newInstance().apply { arguments = params } }
            NavigationTree.Ar -> FragmentScreen { ARFragment.newInstance().apply { arguments = params } }
        }
    }
}
