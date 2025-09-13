package com.poroshin.rut.ar.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.github.terrakok.cicerone.NavigatorHolder
import com.poroshin.rut.ar.common.core.NavigationTree
import com.poroshin.rut.ar.common.core.Navigator
import com.poroshin.rut.ar.common.umbrella.navigation.FlowRouter
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    private val navigatorHolder: NavigatorHolder by inject()
    private val navigator: com.poroshin.rut.ar.common.core.Navigator by inject()
    private val router: FlowRouter by inject()

    private val containerId: Int = View.generateViewId()
    private val appNavigator by lazy { AppNavigator(this, containerId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    FragmentContainerView(context).apply {
                        id = containerId
                    }
                },
            )
            LaunchedEffect(Unit) {
                if (savedInstanceState == null) {
                    navigator.newRootScreen(router, com.poroshin.rut.ar.common.core.NavigationTree.Plp)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(appNavigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }
}
