package com.poroshin.rut.ar.common.pdp.presentation.model

import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.mvi.UiAction
import com.poroshin.rut.ar.common.pdp.domain.ArPlacement
import kotlinx.io.files.Path

sealed class PdpAction : UiAction {
    data class OpenArObject(
        val filePath: Path,
        val width: Float,
        val height: Float,
        val depth: Float,
        val placement: ArPlacement,
        val cartSnapshot: CartItemSnapshot?,
    ) : PdpAction()

    data class OpenArCovering(
        val isFloor: Boolean,
        val patternUrl: String,
    ) : PdpAction()

    data class ShowError(val message: String) : PdpAction()
}
