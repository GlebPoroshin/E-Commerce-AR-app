package com.poroshin.rut.ar.common.pdp.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpState

@Composable
fun PdpScreen(
    state: PdpState,
    onModelLoadClick: (PdpState.Content) -> Unit,
    onDeleteModelClick: (PdpState.Content) -> Unit,
) {
    when (state) {
        is PdpState.Loading -> {
            PdpLoading(modifier = Modifier.fillMaxSize())
        }

        is PdpState.Content -> {
            PdpContent(
                state = state,
                onModelLoadClick = onModelLoadClick,
                onDeleteModelClick = onDeleteModelClick,
            )
        }
    }
}

@Composable
private fun PdpLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PdpContent(
    state: PdpState.Content,
    onModelLoadClick: (PdpState.Content) -> Unit,
    onDeleteModelClick: (PdpState.Content) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Product Detail",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "SKU: ${state.product.sku}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = state.product.name,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "Price: ${state.product.price}",
            style = MaterialTheme.typography.bodyMedium,
        )

        state.loadingState?.let { percent ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Loading: $percent%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.isModelExists) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onModelLoadClick(state) }
                ) {
                    Text(text = "Посмотреть в AR")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onDeleteModelClick(state) }
                ) {
                    Text(text = "Удалить модель")
                }
            }
        } else {
            Button(
                onClick = { onModelLoadClick(state) }
            ) {
                Text(text = "Скачать модель")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
