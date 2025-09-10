package com.poroshin.rut.ar.android

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.github.terrakok.cicerone.NavigatorHolder
import com.poroshin.rut.ar.common.umbrella.navigation.NavigationTree
import com.poroshin.rut.ar.common.umbrella.navigation.Navigator
import com.poroshin.rut.ar.common.umbrella.navigation.FlowRouter
import com.poroshin.rut.ar.common.umbrella.navigation.PlaceholderFragment
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    private val navigatorHolder: NavigatorHolder by inject()
    private val navigator: Navigator by inject()
    private val router: FlowRouter by inject()

    private val appNavigator by lazy { AppNavigator(this, R.id.container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigator.newRootScreen(router, NavigationTree.Plp, Bundle())
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(appNavigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }
}
