package com.poroshin.rut.ar.common.pdp.presentation.model

import com.poroshin.rut.ar.common.mvi.UiAction

sealed class PdpAction : UiAction {
    data class OpenArViewer(val sku: Long) : PdpAction()
}


