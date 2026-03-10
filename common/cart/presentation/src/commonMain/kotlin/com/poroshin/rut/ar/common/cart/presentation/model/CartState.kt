package com.poroshin.rut.ar.common.cart.presentation.model

import com.poroshin.rut.ar.common.cart.domain.CartLine
import com.poroshin.rut.ar.common.cart.domain.CartSummary
import com.poroshin.rut.ar.common.mvi.UiState

sealed class CartState : UiState {
    data object Loading : CartState()

    data class Content(
        val lines: List<CartLine>,
        val summary: CartSummary,
        val badgeCount: Int,
    ) : CartState()
}
