package com.poroshin.rut.ar.common.pdp.presentation

import androidx.lifecycle.viewModelScope
import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.cart.domain.usecase.AddOrIncrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.DecrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveSkuQuantityUseCase
import com.poroshin.rut.ar.common.mvi.SharedViewModel
import com.poroshin.rut.ar.common.pdp.domain.GetPdpParams
import com.poroshin.rut.ar.common.pdp.domain.ProductPageInfo
import com.poroshin.rut.ar.common.pdp.domain.usecase.CheckModelExistsUseCase
import com.poroshin.rut.ar.common.pdp.domain.usecase.DeleteProductModelUseCase
import com.poroshin.rut.ar.common.pdp.domain.usecase.DownloadProductModelUseCase
import com.poroshin.rut.ar.common.pdp.domain.usecase.GetProductPageInfoUseCase
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpAction
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpEvent
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PdpViewModel(
    private val getProductPageInfo: GetProductPageInfoUseCase,
    private val downloadProductModelUseCase: DownloadProductModelUseCase,
    private val checkModelExistsUseCase: CheckModelExistsUseCase,
    private val deleteProductModelUseCase: DeleteProductModelUseCase,
    private val observeSkuQuantityUseCase: ObserveSkuQuantityUseCase,
    private val addOrIncrementCartItemUseCase: AddOrIncrementCartItemUseCase,
    private val decrementCartItemUseCase: DecrementCartItemUseCase,
) : SharedViewModel<PdpState, PdpEvent, PdpAction>(initialState = PdpState.Loading) {

    private object Resolver : KoinComponent

    constructor() : this(
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
        Resolver.get(),
    )

    private var sku: Long? = null
    private var quantityJob: Job? = null

    override suspend fun handleEvent(event: PdpEvent) {
        when (event) {
            is PdpEvent.OnCreate -> {
                sku = event.sku
                observeCartQuantity(event.sku)
                load(event.sku)
            }

            is PdpEvent.OnRetry -> sku?.let { load(it) }

            is PdpEvent.OnModelLoad -> loadModel(event.state)
            is PdpEvent.OnDeleteModel -> deleteModel(event.sku)
            is PdpEvent.OnResume -> sku?.let { checkModel(it) }
            is PdpEvent.OnIncreaseCart -> addToCart(event.snapshot)
            is PdpEvent.OnDecreaseCart -> decrementCart(event.sku)
        }
    }

    private fun load(sku: Long) {
        viewModelScope.launch {
            val product = getProductPageInfo(GetPdpParams(sku))
            val isModelExists = checkModelExistsUseCase(sku)
            val currentQuantity = (viewState.value as? PdpState.Content)?.cartQuantity ?: 0
            updateState {
                PdpState.Content(
                    product = product,
                    isModelExists = isModelExists,
                    cartQuantity = currentQuantity,
                )
            }
        }
    }

    private fun observeCartQuantity(sku: Long) {
        quantityJob?.cancel()

        quantityJob = viewModelScope.launch {
            observeSkuQuantityUseCase(sku).collect { quantity ->
                val currentState = viewState.value
                if (currentState is PdpState.Content && currentState.product.sku == sku) {
                    updateState { currentState.copy(cartQuantity = quantity) }
                }
            }
        }
    }

    private fun addToCart(snapshot: CartItemSnapshot) {
        viewModelScope.launch {
            addOrIncrementCartItemUseCase(snapshot)
        }
    }

    private fun decrementCart(sku: Long) {
        viewModelScope.launch {
            decrementCartItemUseCase(sku)
        }
    }

    private fun checkModel(sku: Long) {
        viewModelScope.launch {
            val currentState = viewState.value
            if (currentState is PdpState.Content) {
                val isModelExists = checkModelExistsUseCase(sku)
                updateState { currentState.copy(isModelExists = isModelExists) }
            }
        }
    }

    private fun deleteModel(sku: Long) {
        viewModelScope.launch {
            deleteProductModelUseCase(sku)
            val currentState = viewState.value
            if (currentState is PdpState.Content) {
                updateState { currentState.copy(isModelExists = false, loadingState = null) }
            }
        }
    }

    private fun loadModel(
        state: PdpState.Content,
    ) {
        val product = state.product
        val arInfo = state.product.ar ?: return

        viewModelScope.launch {
            val path = downloadProductModelUseCase(
                sku = product.sku,
                url = arInfo.arResourceUrl,
                version = arInfo.version ?: 1,
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

            val finalState = viewState.value
            if (finalState is PdpState.Content) {
                updateState {
                    finalState.copy(
                        loadingState = null,
                        isModelExists = true,
                    )
                }
            }

            sendAction(
                PdpAction.OpenArObject(
                    filePath = path,
                    width = arInfo.width,
                    height = arInfo.height,
                    depth = resolveDepthMm(arInfo.width, arInfo.height, arInfo.depth),
                    placement = arInfo.placement,
                    cartSnapshot = product.toCartSnapshot(),
                )
            )
        }
    }

    private fun ProductPageInfo.toCartSnapshot(): CartItemSnapshot {
        return CartItemSnapshot(
            sku = sku,
            name = name,
            priceText = price,
            imageUrl = images.firstOrNull().orEmpty(),
        )
    }

    private fun resolveDepthMm(widthMm: Float, heightMm: Float, depthMm: Float?): Float {
        val fallback = minOf(widthMm, heightMm).coerceAtLeast(1f)
        return depthMm?.takeIf { it > 0f } ?: fallback
    }
}
