package com.poroshin.rut.ar.common.plp.presentation

import com.poroshin.rut.ar.common.mvi.SharedViewModel
import com.poroshin.rut.ar.common.mvi.UiAction
import com.poroshin.rut.ar.common.mvi.UiEvent
import com.poroshin.rut.ar.common.mvi.UiState

data class PlpState(
    val counter: Int = 0,
) : UiState

sealed class PlpEvent : UiEvent {
    data object Increment : PlpEvent()
    data object Decrement : PlpEvent()
}

sealed class PlpAction : UiAction {
    data object ShowLimitToast : PlpAction()
}

class PlpTestViewModel : SharedViewModel<PlpState, PlpEvent, PlpAction>(
    initialState = PlpState()
) {
    override suspend fun handleEvent(event: PlpEvent) {
        when (event) {
            PlpEvent.Increment -> updateState {
                val next = counter + 1
                if (next > 10) {
                    sendAction(PlpAction.ShowLimitToast)
                    this
                } else copy(counter = next)
            }
            PlpEvent.Decrement -> updateState {
                copy(counter = (counter - 1).coerceAtLeast(0))
            }
        }
    }
}


