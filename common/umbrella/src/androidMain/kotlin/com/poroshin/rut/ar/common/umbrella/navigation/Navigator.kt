package com.poroshin.rut.ar.common.umbrella.navigation

import android.os.Bundle
import com.github.terrakok.cicerone.Router

interface Navigator {
    fun navigateTo(router: Router, key: NavigationTree, params: Bundle = Bundle())
    fun startFlow(router: Router, key: NavigationTree, params: Bundle = Bundle())
    fun newRootScreen(router: Router, key: NavigationTree, params: Bundle = Bundle())
}


