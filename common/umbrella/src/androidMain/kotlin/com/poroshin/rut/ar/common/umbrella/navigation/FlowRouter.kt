package com.poroshin.rut.ar.common.umbrella.navigation

import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen

/**
 * Router-обёртка, которая перенаправляет команды навигации на `parentRouter`, если тот задан.
 *
 * Используется для организации независимых flow'ов, где есть родительский Router (например, в Activity) и дочерние FlowRouter'ы для модулей.
 *
 * @param parentRouter если не null — все команды будут выполняться на нём, иначе — на этом экземпляре.
 */
class FlowRouter(private val parentRouter: Router?) : Router() {

    /**
     * Запускает flow, навигируя на указанный экран.
     *
     * @param screen экран, с которого начинается flow.
     */
    fun startFlow(screen: Screen) {
        runCommand { navigateTo(screen) }
    }

    /**
     * Заменяет корень навигации на переданный экран — запуск нового корня flow'а.
     *
     * @param screen экран, который станет новым корнем.
     */
    fun newRootFlow(screen: Screen) {
        runCommand { newRootScreen(screen) }
    }

    /**
     * Завершает текущий flow (вызывает `exit()`).
     */
    fun finishFlow() {
        runCommand { exit() }
    }

    /**
     * Выполняет "pop" — один шаг назад (эквивалент Router.exit()).
     */
    fun pop() {
        runCommand { exit() }
    }

    /**
     * Выполняет pop до указанного экрана (backTo).
     *
     * @param screen экран, до которого нужно откатиться. Если экран не найден в стеке — поведение Router'а.
     */
    fun popTo(screen: Screen) {
        runCommand { backTo(screen) }
    }

    /**
     * Выполняет pop до корня навигации. Использует `backTo(null)` — откат на самый корень.
     */
    fun popToRoot() {
        runCommand { backTo(null) }
    }

    /**
     * Выполняет команду навигации — на `parentRouter`, если он задан, иначе на этом экземпляре.
     *
     * @param command лямбда с вызовами Router.* (navigateTo, newRootScreen, exit и т.д.).
     */
    private fun runCommand(command: Router.() -> Unit) {
        if (parentRouter != null) parentRouter.command() else this.command()
    }
}
