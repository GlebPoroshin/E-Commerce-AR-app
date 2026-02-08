package com.poroshin.rut.ar.common.cart.presentation.model

import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.mvi.UiAction
import com.poroshin.rut.ar.common.mvi.UiEvent
import com.poroshin.rut.ar.common.mvi.UiState

sealed class CartQuantityState : UiState {
    data object Hidden : CartQuantityState()

    data class Content(
        val snapshot: CartItemSnapshot,
        val quantity: Int,
    ) : CartQuantityState()
}

sealed class CartQuantityEvent : UiEvent {
    data class SetSnapshot(val snapshot: CartItemSnapshot?) : CartQuantityEvent()

    data object OnIncrease : CartQuantityEvent()

    data object OnDecrease : CartQuantityEvent()
}

sealed class CartQuantityAction : UiAction
