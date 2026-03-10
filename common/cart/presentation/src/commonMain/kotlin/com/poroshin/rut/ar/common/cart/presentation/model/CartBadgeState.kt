package com.poroshin.rut.ar.common.cart.presentation.model

import com.poroshin.rut.ar.common.cart.domain.CartBadgeFormatter
import com.poroshin.rut.ar.common.mvi.UiAction
import com.poroshin.rut.ar.common.mvi.UiEvent
import com.poroshin.rut.ar.common.mvi.UiState

data class CartBadgeState(
    val count: Int = 0,
    val text: String? = CartBadgeFormatter.format(0),
) : UiState {
    companion object {
        fun fromCount(count: Int): CartBadgeState {
            return CartBadgeState(
                count = count,
                text = CartBadgeFormatter.format(count),
            )
        }
    }
}

sealed class CartBadgeEvent : UiEvent {
    data object OnCreate : CartBadgeEvent()
}

sealed class CartBadgeAction : UiAction
