package com.poroshin.rut.ar.common.cart.presentation.model

import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.mvi.UiEvent

sealed class CartEvent : UiEvent {
    data object OnCreate : CartEvent()

    data class OnIncrease(val snapshot: CartItemSnapshot) : CartEvent()

    data class OnDecrease(val sku: Long) : CartEvent()

    data class OnRemove(val sku: Long) : CartEvent()

    data class OnItemClick(val sku: Long) : CartEvent()

    data object OnMainClick : CartEvent()
}
