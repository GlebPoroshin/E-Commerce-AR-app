package com.poroshin.rut.ar.common.pdp.presentation.model

import com.poroshin.rut.ar.common.mvi.UiEvent

sealed class PdpEvent : UiEvent {
    data class OnCreate(val sku: Long) : PdpEvent()

    class OnModelLoad(val state: PdpState.Content) : PdpEvent()

    data object OnRetry : PdpEvent()
}


