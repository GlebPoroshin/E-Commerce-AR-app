package com.poroshin.rut.ar.android

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.poroshin.rut.ar.common.ar.presentation.ARFragment
import com.poroshin.rut.ar.common.cart.domain.CartBadgeFormatter
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartBadgeCountUseCase
import com.poroshin.rut.ar.common.cart.presentation.CartFragment
import com.poroshin.rut.ar.common.core.NavigationTree
import com.poroshin.rut.ar.common.core.Navigator
import com.poroshin.rut.ar.common.umbrella.navigation.FlowRouter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.ext.android.inject

private enum class AppTab {
    Main,
    Cart,
}

class MainActivity : FragmentActivity(), CartFragment.Host {

    private val navigatorHolder: NavigatorHolder by inject()
    private val navigator: Navigator by inject()
    private val router: FlowRouter by inject()
    private val observeCartBadgeCountUseCase: ObserveCartBadgeCountUseCase by inject()

    private val mainContainerId: Int = View.generateViewId()
    private val cartContainerId: Int = View.generateViewId()
    private val appNavigator by lazy { AppNavigator(this, mainContainerId) }

    private val selectedTab = MutableStateFlow(AppTab.Main)
    private val bottomBarVisible = MutableStateFlow(true)
    private var isNavigationInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerFragmentCallbacks()

        setContent {
            MyApplicationTheme {
                MainScaffold(
                    selectedTabFlow = selectedTab,
                    bottomBarVisibleFlow = bottomBarVisible,
                    observeCartBadgeCountUseCase = observeCartBadgeCountUseCase,
                    mainContainerId = mainContainerId,
                    cartContainerId = cartContainerId,
                    onTabSelected = { tab ->
                        selectedTab.value = tab
                        updateBottomBarVisibility()
                    },
                    onContainersReady = {
                        initializeNavigationIfNeeded(savedInstanceState)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(appNavigator)
        updateBottomBarVisibility()
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun openMainTab() {
        selectedTab.value = AppTab.Main
        updateBottomBarVisibility()
    }

    override fun openMainProduct(sku: Long) {
        selectedTab.value = AppTab.Main
        updateBottomBarVisibility()
        navigator.navigateTo(
            router = router,
            key = NavigationTree.Pdp,
            params = Bundle().apply { putLong("sku", sku) },
        )
    }

    private fun ensureCartRoot() {
        if (supportFragmentManager.findFragmentById(cartContainerId) == null) {
            supportFragmentManager.commitNow {
                replace(cartContainerId, CartFragment.newInstance(), "cart.root")
            }
        }
    }

    private fun initializeNavigationIfNeeded(savedInstanceState: Bundle?) {
        if (isNavigationInitialized) return
        if (findViewById<View?>(mainContainerId) == null) return
        if (findViewById<View?>(cartContainerId) == null) return

        ensureCartRoot()
        if (savedInstanceState == null && supportFragmentManager.findFragmentById(mainContainerId) == null) {
            navigator.newRootScreen(router, NavigationTree.Plp)
        }
        updateBottomBarVisibility()
        isNavigationInitialized = true
    }

    private fun registerFragmentCallbacks() {
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                    if (fm == supportFragmentManager) {
                        updateBottomBarVisibility()
                    }
                }

                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    if (fm == supportFragmentManager) {
                        updateBottomBarVisibility()
                    }
                }
            },
            true,
        )
    }

    private fun updateBottomBarVisibility() {
        val isMainTabSelected = selectedTab.value == AppTab.Main
        val isArVisible = supportFragmentManager.findFragmentById(mainContainerId) is ARFragment

        bottomBarVisible.value = !(isMainTabSelected && isArVisible)
    }
}

@Composable
private fun MainScaffold(
    selectedTabFlow: StateFlow<AppTab>,
    bottomBarVisibleFlow: StateFlow<Boolean>,
    observeCartBadgeCountUseCase: ObserveCartBadgeCountUseCase,
    mainContainerId: Int,
    cartContainerId: Int,
    onTabSelected: (AppTab) -> Unit,
    onContainersReady: () -> Unit,
) {
    val selectedTab by selectedTabFlow.collectAsState()
    val bottomBarVisible by bottomBarVisibleFlow.collectAsState()
    val badgeCount by observeCartBadgeCountUseCase().collectAsState(initial = 0)
    val badgeText = CartBadgeFormatter.format(badgeCount)

    Scaffold(
        bottomBar = {
            if (bottomBarVisible) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == AppTab.Main,
                        onClick = { onTabSelected(AppTab.Main) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Главная",
                            )
                        },
                        label = { Text("Главная") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.Cart,
                        onClick = { onTabSelected(AppTab.Cart) },
                        icon = {
                            if (badgeText != null) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            modifier = Modifier.offset(x = 6.dp, y = (-4).dp),
                                        ) {
                                            Text(badgeText)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingCart,
                                        contentDescription = "Корзина",
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Корзина",
                                )
                            }
                        },
                        label = { Text("Корзина") },
                    )
                }
            }
        },
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                FrameLayout(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )

                    val mainContainer = FragmentContainerView(context).apply {
                        id = mainContainerId
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                        )
                    }
                    addView(mainContainer)

                    val cartContainer = FragmentContainerView(context).apply {
                        id = cartContainerId
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                        )
                    }
                    addView(cartContainer)
                }
            },
            update = { root ->
                if (root.isAttachedToWindow) {
                    onContainersReady()
                } else {
                    root.post { onContainersReady() }
                }

                val mainContainer = root.findViewById<View>(mainContainerId)
                val cartContainer = root.findViewById<View>(cartContainerId)

                mainContainer?.isVisible = selectedTab == AppTab.Main
                cartContainer?.isVisible = selectedTab == AppTab.Cart
            },
        )
    }
}
