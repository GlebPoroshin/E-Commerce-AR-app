package com.poroshin.rut.ar.common.pdp.presentation.model

import com.poroshin.rut.ar.common.mvi.UiState
import com.poroshin.rut.ar.common.pdp.domain.ProductPageInfo

sealed class PdpState : UiState {
    data object Loading : PdpState()

    data class Content(
        val loadingState: Int? = null,
        val product: ProductPageInfo,
    ) : PdpState()

    data class Error(val message: String) : PdpState()
}
