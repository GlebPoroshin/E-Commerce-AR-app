package com.poroshin.rut.ar.common.cart.presentation.model

import com.poroshin.rut.ar.common.mvi.UiAction

sealed class CartAction : UiAction {
    data class OpenPdp(val sku: Long) : CartAction()

    data object SwitchToMainTab : CartAction()
}
