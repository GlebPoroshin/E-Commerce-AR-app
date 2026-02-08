package com.poroshin.rut.ar.common.cart.presentation

import androidx.lifecycle.viewModelScope
import com.poroshin.rut.ar.common.cart.domain.usecase.AddOrIncrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.DecrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartBadgeCountUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartLinesUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartSummaryUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.RemoveCartItemUseCase
import com.poroshin.rut.ar.common.cart.presentation.model.CartAction
import com.poroshin.rut.ar.common.cart.presentation.model.CartEvent
import com.poroshin.rut.ar.common.cart.presentation.model.CartState
import com.poroshin.rut.ar.common.mvi.SharedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class CartViewModel(
    private val observeCartLinesUseCase: ObserveCartLinesUseCase,
    private val observeCartSummaryUseCase: ObserveCartSummaryUseCase,
    private val observeCartBadgeCountUseCase: ObserveCartBadgeCountUseCase,
    private val addOrIncrementCartItemUseCase: AddOrIncrementCartItemUseCase,
    private val decrementCartItemUseCase: DecrementCartItemUseCase,
    private val removeCartItemUseCase: RemoveCartItemUseCase,
) : SharedViewModel<CartState, CartEvent, CartAction>(initialState = CartState.Loading) {

    private object Resolver : KoinComponent

    constructor() : this(
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
    )

    private var observeJob: Job? = null

    override suspend fun handleEvent(event: CartEvent) {
        when (event) {
            is CartEvent.OnCreate -> observe()
            is CartEvent.OnIncrease -> increment(event)
            is CartEvent.OnDecrease -> decrement(event)
            is CartEvent.OnRemove -> remove(event)
            is CartEvent.OnItemClick -> sendAction(CartAction.OpenPdp(event.sku))
            is CartEvent.OnMainClick -> sendAction(CartAction.SwitchToMainTab)
        }
    }

    private fun observe() {
        if (observeJob != null) return

        observeJob = viewModelScope.launch {
            combine(
                observeCartLinesUseCase(),
                observeCartSummaryUseCase(),
                observeCartBadgeCountUseCase(),
            ) { lines, summary, badgeCount ->
                CartState.Content(
                    lines = lines,
                    summary = summary,
                    badgeCount = badgeCount,
                )
            }.collect { content ->
                updateState { content }
            }
        }
    }

    private fun increment(event: CartEvent.OnIncrease) {
        viewModelScope.launch {
            addOrIncrementCartItemUseCase(event.snapshot)
        }
    }

    private fun decrement(event: CartEvent.OnDecrease) {
        viewModelScope.launch {
            decrementCartItemUseCase(event.sku)
        }
    }

    private fun remove(event: CartEvent.OnRemove) {
        viewModelScope.launch {
            removeCartItemUseCase(event.sku)
        }
    }
}
