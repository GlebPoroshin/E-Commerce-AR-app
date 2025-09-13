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
 * Реализация [Navigator], которая преобразует [NavigationTree] в конкретные [Screen] (фрагменты).
 *
 * Если в будущем понадобится передавать параметры в фрагменты — добавьте их в `Bundle` внутри
 * `getFragmentScreen` (в текущей реализации `params` не используются при создании фрагментов).
 */
class NavigatorImpl : Navigator {

    /**
     * Выполняет navigateTo с экраном, соответствующим [key].
     */
    override fun navigateTo(router: Router, key: NavigationTree, params: Bundle) {
        router.navigateTo(getFragmentScreen(key, params))
    }

    /**
     * Если [router] — [FlowRouter], вызывает `startFlow`, иначе — обычный `navigateTo`.
     */
    override fun startFlow(router: Router, key: NavigationTree, params: Bundle) {
        (router as? FlowRouter)?.startFlow(getFragmentScreen(key, params))
            ?: router.navigateTo(getFragmentScreen(key, params))
    }

    /**
     * Заменяет корень навигации на экран, соответствующий [key].
     */
    override fun newRootScreen(router: Router, key: NavigationTree, params: Bundle) {
        router.newRootScreen(getFragmentScreen(key, params))
    }

    /**
     * Выполнить "pop" — шаг назад.
     */
    override fun pop(router: Router) {
        router.exit()
    }

    /**
     * Выполнить pop до экрана, соответствующего [key].
     */
    override fun popTo(router: Router, key: NavigationTree, params: Bundle) {
        router.backTo(getFragmentScreen(key, params))
    }

    /**
     * Выполнить pop до корня навигации.
     */
    override fun popToRoot(router: Router) {
        router.backTo(null)
    }

    /**
     * Маппинг [NavigationTree] -> [Screen].
     *
     * @param key ключ навигации.
     * @param params дополнительные параметры (в текущей реализации не применяются, но оставлены для расширения).
     * @return [Screen] с соответствующим фрагментом.
     */
    private fun getFragmentScreen(key: NavigationTree, params: Bundle): Screen {
        return when (key) {
            NavigationTree.Plp -> FragmentScreen { PlpFragment.newInstance().apply { arguments = params } }
            NavigationTree.Pdp -> FragmentScreen { PdpFragment.newInstance().apply { arguments = params } }
            NavigationTree.Ar -> FragmentScreen { ARFragment.newInstance().apply { arguments = params } }
        }
    }
}
