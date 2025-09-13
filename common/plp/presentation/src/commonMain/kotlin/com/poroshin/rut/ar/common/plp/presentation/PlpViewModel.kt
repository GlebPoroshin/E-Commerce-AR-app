package com.poroshin.rut.ar.common.plp.presentation

import androidx.lifecycle.viewModelScope
import com.poroshin.rut.ar.common.mvi.SharedViewModel
import com.poroshin.rut.ar.common.plp.domain.GetPlpProductsUseCase
import com.poroshin.rut.ar.common.plp.presentation.model.PlpAction
import com.poroshin.rut.ar.common.plp.presentation.model.PlpEvent
import com.poroshin.rut.ar.common.plp.presentation.model.PlpState
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PlpViewModel(
    private val getPlpProductsUseCase: GetPlpProductsUseCase,
) : SharedViewModel<PlpState, PlpEvent, PlpAction>(initialState = PlpState.Loading) {

    private object Resolver : KoinComponent
    constructor() : this(Resolver.get())

    private val loadedPage: Int? = null

    override suspend fun handleEvent(event: PlpEvent) {
        when (event) {
            is PlpEvent.OnCreate -> handleOnCreate()

            is PlpEvent.OnProductClick -> sendAction(PlpAction.OpenPdp(event.sku))
        }
    }

    private fun handleOnCreate() {
        viewModelScope.launch {
            val loadedItems = getPlpProductsUseCase(loadedPage)
            updateState {
                PlpState.Content(
                   items = loadedItems
                )
            }
        }
    }
}
