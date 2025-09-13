package com.poroshin.rut.ar.common.pdp.presentation

import androidx.lifecycle.viewModelScope
import com.poroshin.rut.ar.common.mvi.SharedViewModel
import com.poroshin.rut.ar.common.pdp.domain.GetPdpParams
import com.poroshin.rut.ar.common.pdp.domain.GetProductPageInfoUseCase
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpAction
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpEvent
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpState
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PdpViewModel(
    private val getProductPageInfo: GetProductPageInfoUseCase,
) : SharedViewModel<PdpState, PdpEvent, PdpAction>(initialState = PdpState.Loading) {

    private object Resolver : KoinComponent
    constructor() : this(Resolver.get())

    private var sku: Long? = null

    override suspend fun handleEvent(event: PdpEvent) {
        when (event) {
            is PdpEvent.OnCreate -> {
                sku = event.sku
                load(event.sku)
            }

            is PdpEvent.OnRetry -> sku?.let { load(it) }
        }
    }

    private fun load(sku: Long) {
        viewModelScope.launch {
            try {
                updateState { PdpState.Loading }
                val product = getProductPageInfo(GetPdpParams(sku))
                updateState { PdpState.Content(product) }
            } catch (t: Throwable) {
                updateState { PdpState.Error(t.message ?: "Unknown error") }
            }
        }
    }
}


