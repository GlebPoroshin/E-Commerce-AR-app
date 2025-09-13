package com.poroshin.rut.ar.common.plp.presentation.model

import com.poroshin.rut.ar.common.mvi.UiAction

sealed class PlpAction : UiAction {
    data class OpenPdp(val sku: Long) : PlpAction()
}