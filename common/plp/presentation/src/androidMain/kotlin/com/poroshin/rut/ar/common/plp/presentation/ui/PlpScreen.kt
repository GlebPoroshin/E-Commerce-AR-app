package com.poroshin.rut.ar.common.plp.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.poroshin.rut.ar.common.plp.domain.Product
import com.poroshin.rut.ar.common.plp.presentation.PlpViewModel
import com.poroshin.rut.ar.common.plp.presentation.model.PlpEvent
import com.poroshin.rut.ar.common.plp.presentation.model.PlpState

@Composable
fun PlpScreen(
    viewModel: PlpViewModel,
) {
    val state by viewModel.viewState.collectAsState()

    when(val viewState = state) {
        is PlpState.Content -> {
            PlpContent(
                state = viewState,
                onProductClick = { sku -> viewModel.onEvent(PlpEvent.OnProductClick(sku)) }
            )
        }

        is PlpState.Loading -> {
            PlpLoading()
        }
    }
}

@Composable
private fun PlpContent(
    state: PlpState.Content,
    onProductClick: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        itemsIndexed(state.items.chunked(2)) { _, pair ->
            val left = pair.getOrNull(0)
            val right = pair.getOrNull(1)
            if (left != null) {
                PairPlpItems(
                    left = left,
                    right = right,
                    onItemClick = { sku -> onProductClick(sku) },
                )
            }
        }
    }
}

@Composable
private fun PlpLoading() {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        items(3) {
            PairPlpItemsShimmer()
        }
    }
}
