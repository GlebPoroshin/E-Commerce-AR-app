package com.poroshin.rut.ar.common.umbrella.navigation

import android.os.Bundle
import com.github.terrakok.cicerone.Router

/**
 * Абстракция навигации для модулей приложения.
 *
 * Реализация переводит логический [NavigationTree] + параметры в конкретные Screen'ы и
 * выполняет команды на переданном [router].
 */
interface Navigator {
    /**
     * Выполнить навигацию (push) на экран, соответствующий [key].
     *
     * @param router Router, на котором выполнять команду.
     * @param key логический ключ экрана из [NavigationTree].
     * @param params дополнительные параметры, по умолчанию пустой [Bundle].
     */
    fun navigateTo(router: Router, key: NavigationTree, params: Bundle = Bundle())

    /**
     * Запустить flow — семантически начать новый поток навигации (если router — [FlowRouter],
     * будет использован `startFlow`, иначе — обычный `navigateTo`).
     *
     * @param router Router, на котором выполнять команду.
     * @param key логический ключ экрана из [NavigationTree].
     * @param params дополнительные параметры, по умолчанию пустой [Bundle].
     */
    fun startFlow(router: Router, key: NavigationTree, params: Bundle = Bundle())

    /**
     * Установить новый корень навигации (newRootScreen).
     *
     * @param router Router, на котором выполнять команду.
     * @param key логический ключ экрана из [NavigationTree].
     * @param params дополнительные параметры, по умолчанию пустой [Bundle].
     */
    fun newRootScreen(router: Router, key: NavigationTree, params: Bundle = Bundle())

    /**
     * Выполнить "pop" — шаг назад.
     *
     * @param router Router, на котором выполнять команду.
     */
    fun pop(router: Router)

    /**
     * Выполнить pop до указанного логического ключа (popTo).
     *
     * @param router Router, на котором выполнять команду.
     * @param key логический ключ экрана из [NavigationTree], до которого нужно откатиться.
     * @param params дополнительные параметры, если нужно сопоставить Screen (по умолчанию пустой Bundle).
     */
    fun popTo(router: Router, key: NavigationTree, params: Bundle = Bundle())

    /**
     * Выполнить pop до корня навигации.
     *
     * @param router Router, на котором выполнять команду.
     */
    fun popToRoot(router: Router)
}
