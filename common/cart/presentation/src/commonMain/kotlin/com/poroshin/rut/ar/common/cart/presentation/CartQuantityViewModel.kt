package com.poroshin.rut.ar.common.cart.presentation

import androidx.lifecycle.viewModelScope
import com.poroshin.rut.ar.common.cart.domain.usecase.AddOrIncrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.DecrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveSkuQuantityUseCase
import com.poroshin.rut.ar.common.cart.presentation.model.CartQuantityAction
import com.poroshin.rut.ar.common.cart.presentation.model.CartQuantityEvent
import com.poroshin.rut.ar.common.cart.presentation.model.CartQuantityState
import com.poroshin.rut.ar.common.mvi.SharedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class CartQuantityViewModel(
    private val observeSkuQuantityUseCase: ObserveSkuQuantityUseCase,
    private val addOrIncrementCartItemUseCase: AddOrIncrementCartItemUseCase,
    private val decrementCartItemUseCase: DecrementCartItemUseCase,
) : SharedViewModel<CartQuantityState, CartQuantityEvent, CartQuantityAction>(
    initialState = CartQuantityState.Hidden,
) {

    private object Resolver : KoinComponent

    constructor() : this(Resolver.get(), Resolver.get(), Resolver.get())

    private var observeJob: Job? = null

    override suspend fun handleEvent(event: CartQuantityEvent) {
        when (event) {
            is CartQuantityEvent.SetSnapshot -> setSnapshot(event.snapshot)
            is CartQuantityEvent.OnIncrease -> increase()
            is CartQuantityEvent.OnDecrease -> decrease()
        }
    }

    private fun setSnapshot(snapshot: com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot?) {
        observeJob?.cancel()

        if (snapshot == null) {
            updateState { CartQuantityState.Hidden }
            return
        }

        observeJob = viewModelScope.launch {
            observeSkuQuantityUseCase(snapshot.sku).collect { quantity ->
                updateState {
                    CartQuantityState.Content(
                        snapshot = snapshot,
                        quantity = quantity,
                    )
                }
            }
        }
    }

    private fun increase() {
        val current = currentState as? CartQuantityState.Content ?: return

        viewModelScope.launch {
            addOrIncrementCartItemUseCase(current.snapshot)
        }
    }

    private fun decrease() {
        val current = currentState as? CartQuantityState.Content ?: return

        viewModelScope.launch {
            decrementCartItemUseCase(current.snapshot.sku)
        }
    }
}
