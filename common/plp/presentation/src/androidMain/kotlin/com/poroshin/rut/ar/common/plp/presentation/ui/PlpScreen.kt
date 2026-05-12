package com.poroshin.rut.ar.common.plp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.poroshin.rut.ar.common.plp.presentation.PlpViewModel
import com.poroshin.rut.ar.common.plp.presentation.model.PlpEvent
import com.poroshin.rut.ar.common.plp.presentation.model.PlpState

@Composable
fun PlpScreen(
    viewModel: PlpViewModel,
) {
    val state by viewModel.viewState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("plp_screen")
    ) {
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

            is PlpState.Error -> {
                PlpError(
                    message = viewState.message,
                    onRetry = { viewModel.onEvent(PlpEvent.OnRetry) }
                )
            }
        }
    }
}

@Composable
private fun PlpContent(
    state: PlpState.Content,
    onProductClick: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("plp_content_list")
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
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(6) {
            PairPlpItemsShimmer()
        }
    }
}

@Composable
private fun PlpError(
    message: String?,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message ?: "Не удалось загрузить каталог",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(text = "Повторить")
        }
    }
}
