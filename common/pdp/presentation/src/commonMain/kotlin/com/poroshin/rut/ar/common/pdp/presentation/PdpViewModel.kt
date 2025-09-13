package com.poroshin.rut.ar.common.pdp.presentation

import androidx.lifecycle.viewModelScope
import com.poroshin.rut.ar.common.mvi.SharedViewModel
import com.poroshin.rut.ar.common.pdp.domain.GetPdpParams
import com.poroshin.rut.ar.common.pdp.domain.usecase.DownloadProductModelUseCase
import com.poroshin.rut.ar.common.pdp.domain.usecase.GetProductPageInfoUseCase
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpAction
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpEvent
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpState
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PdpViewModel(
    private val getProductPageInfo: GetProductPageInfoUseCase,
    private val downloadProductModelUseCase: DownloadProductModelUseCase,
) : SharedViewModel<PdpState, PdpEvent, PdpAction>(initialState = PdpState.Loading) {

    private object Resolver : KoinComponent

    constructor() : this(Resolver.get(), Resolver.get())

    private var sku: Long? = null

    override suspend fun handleEvent(event: PdpEvent) {
        when (event) {
            is PdpEvent.OnCreate -> {
                sku = event.sku
                load(event.sku)
            }

            is PdpEvent.OnRetry -> sku?.let { load(it) }

            is PdpEvent.OnModelLoad -> loadModel(
                sku = event.sku,
                url = event.url,
                version = event.version,
            )
        }
    }

    private fun load(sku: Long) {
        viewModelScope.launch {
            val product = getProductPageInfo(GetPdpParams(sku))
            updateState { PdpState.Content(product = product) }
        }
    }

    private fun loadModel(
        sku: Long,
        url: String,
        version: Int,
    ) {
        viewModelScope.launch {
            downloadProductModelUseCase(
                sku = sku,
                url = url,
                version = version,
                onProgress = { received, total ->
                    if (total != null && total > 0L) {
                        val percent = ((received.toDouble() / total.toDouble()) * 100.0)
                            .toInt()
                            .coerceIn(0, 100)
                        val current = viewState.value
                        if (current is PdpState.Content) {
                            updateState { current.copy(loadingState = percent) }
                        }
                    }
                }
            )
        }
    }
}
