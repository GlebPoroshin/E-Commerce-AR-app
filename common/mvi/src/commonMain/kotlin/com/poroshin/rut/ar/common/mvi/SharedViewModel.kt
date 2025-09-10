package com.poroshin.rut.ar.common.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Базовый KMM ViewModel для presentation слоя.
 * Спроектирована в соответствии с паттерном MVI+UDF.
 *
 * Содержит три потока данных:
 *  - [viewState]  — состояние экрана. Извне можно подписаться только на чтение.
 *  - [viewAction] — одноразовые действия (навигация, сообщения, диалоги и т.п.; только чтение).
 *  - входящие события от UI принимаются только через [onEvent]; прямого доступа к потоку событий нет.
 *
 * Инварианты инкапсуляции:
 *  - Извне разрешено только: читать [viewState]/[viewAction] и вызывать [onEvent].
 *  - Менять состояние/генерировать действия можно только внутри через [updateState] и [sendAction].
 *
 * @param S тип состояния (обычно data class).
 * @param E тип входящих событий(намерений) от UI.
 * @param A тип действий для UI.
 * @param initialState стартовое состояние viewmodel.
 */
abstract class SharedViewModel<S : UiState, E : UiEvent, A : UiAction>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val viewState: StateFlow<S> = _state.asStateFlow()

    private val _actions = MutableSharedFlow<A>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val viewAction: SharedFlow<A> = _actions.asSharedFlow()

    private val _events = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    init {
        viewModelScope.launch {
            _events.collect { handleEvent(it) }
        }
    }

    /**
     * Ручка, с помощью которою события из UI эмитятся в поток.
     * Событие улетает в корутину обработчика [handleEvent].
     */
    fun onEvent(event: E) {
        if (!_events.tryEmit(event)) {
            viewModelScope.launch { _events.emit(event) }
        }
    }

    /**
     * Изменить/обновить ViewState.
     * Используется только внутри самой viewmodel.
     */
    protected fun updateState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    /**
     * Отправить ViewAction UI.
     * Использовать для навигации/сообщений/диалогов.
     */
    protected fun sendAction(action: A) {
        if (!_actions.tryEmit(action)) {
            viewModelScope.launch { _actions.emit(action) }
        }
    }

    /**
     * Текущее состояние (для чтения внутри VM).
     */
    protected val currentState: S get() = _state.value

    /**
     * Обработка событий UI. Здесь меняем state и шлём actions.
     * Либо вызываем handler'ы - делегаты, передавай в них ViewEvent.
     */
    protected abstract suspend fun handleEvent(event: E)
}
