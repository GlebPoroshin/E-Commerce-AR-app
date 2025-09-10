package com.poroshin.rut.ar.common.mvi

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.DisposableHandle

/**
 * Хелпер для iOS: подписаться на Flow на главном потоке и получить handle для dispose().
 *
 * Использование на Swift:
 *   let handle = vm.viewState.watch { state in ... }
 *   handle.dispose()
 */
fun <T> Flow<T>.watch(onEach: (T) -> Unit): DisposableHandle {
    val scope = MainScope()
    val job = scope.launch(Dispatchers.Main.immediate) {
        this@watch
            .onCompletion { /* no-op */ }
            .collect { onEach(it) }
    }
    return DisposableHandle { job.cancel() }
}

/**
 * Возвращает единый DisposableHandle и на state, и на action(обе подписки закроются разом).
 */
fun <S : Any, A : Any> bind(
    state: Flow<S>,
    onState: (S) -> Unit,
    action: Flow<A>,
    onAction: (A) -> Unit
): DisposableHandle {
    val s = state.watch(onState)
    val a = action.watch(onAction)
    return DisposableHandle {
        s.dispose()
        a.dispose()
    }
}
