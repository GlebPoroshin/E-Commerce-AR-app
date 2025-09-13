package com.poroshin.rut.ar.common.plp.presentation.model

import com.poroshin.rut.ar.common.mvi.UiEvent

sealed class PlpEvent : UiEvent {
    data object OnCreate: PlpEvent()
    class OnProductClick(val sku: Long) : PlpEvent()
}