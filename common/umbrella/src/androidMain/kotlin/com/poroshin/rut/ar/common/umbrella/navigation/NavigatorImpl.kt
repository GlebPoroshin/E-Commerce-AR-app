package com.poroshin.rut.ar.common.umbrella.navigation

import android.os.Bundle
import com.github.terrakok.cicerone.Screen
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.FragmentScreen

class NavigatorImpl : Navigator {

    override fun navigateTo(router: Router, key: NavigationTree, params: Bundle) {
        router.navigateTo(getFragmentScreen(key, params))
    }

    override fun startFlow(router: Router, key: NavigationTree, params: Bundle) {
        (router as? FlowRouter)?.startFlow(getFragmentScreen(key, params))
            ?: router.navigateTo(getFragmentScreen(key, params))
    }

    override fun newRootScreen(router: Router, key: NavigationTree, params: Bundle) {
        router.newRootScreen(getFragmentScreen(key, params))
    }

    private fun getFragmentScreen(key: NavigationTree, params: Bundle): Screen {
        return when (key) {
            NavigationTree.Plp -> FragmentScreen {  }
            NavigationTree.Pdp -> FragmentScreen {  }
            NavigationTree.Ar -> FragmentScreen {  }
        }
    }
}


