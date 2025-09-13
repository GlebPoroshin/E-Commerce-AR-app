package com.poroshin.rut.ar.common.plp.presentation.model

import com.poroshin.rut.ar.common.mvi.UiState
import com.poroshin.rut.ar.common.plp.domain.Product

sealed class PlpState : UiState {
    data object Loading : PlpState()

    class Content(val items: List<Product>) : PlpState()
}
