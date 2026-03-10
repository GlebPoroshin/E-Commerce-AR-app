package com.poroshin.rut.ar.common.cart.presentation

import androidx.lifecycle.viewModelScope
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartBadgeCountUseCase
import com.poroshin.rut.ar.common.cart.presentation.model.CartBadgeAction
import com.poroshin.rut.ar.common.cart.presentation.model.CartBadgeEvent
import com.poroshin.rut.ar.common.cart.presentation.model.CartBadgeState
import com.poroshin.rut.ar.common.mvi.SharedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class CartBadgeViewModel(
    private val observeCartBadgeCountUseCase: ObserveCartBadgeCountUseCase,
) : SharedViewModel<CartBadgeState, CartBadgeEvent, CartBadgeAction>(
    initialState = CartBadgeState.fromCount(0),
) {

    private object Resolver : KoinComponent

    constructor() : this(Resolver.get())

    private var observeJob: Job? = null

    override suspend fun handleEvent(event: CartBadgeEvent) {
        when (event) {
            is CartBadgeEvent.OnCreate -> observe()
        }
    }

    private fun observe() {
        if (observeJob != null) return

        observeJob = viewModelScope.launch {
            observeCartBadgeCountUseCase()
                .catch { emit(0) }
                .collect { count ->
                updateState { CartBadgeState.fromCount(count) }
                }
        }
    }
}
