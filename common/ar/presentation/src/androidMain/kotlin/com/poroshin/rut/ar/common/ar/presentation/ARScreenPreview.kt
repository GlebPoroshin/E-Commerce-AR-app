package com.poroshin.rut.ar.common.ar.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.poroshin.rut.ar.common.ar.domain.ArTrackingStatus

@Preview(name = "AR — Tracking", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ArScreenPreviewTracking() {
    ArScreenPreviewScaffold(
        isSingleMode = true,
        placedModels = 1,
        isModelLoading = false,
        trackingStatus = ArTrackingStatus.Tracking,
        errorMessage = null,
        cartQuantity = 1,
    )
}

@Preview(name = "AR — Searching", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ArScreenPreviewSearching() {
    ArScreenPreviewScaffold(
        isSingleMode = true,
        placedModels = 0,
        isModelLoading = true,
        trackingStatus = ArTrackingStatus.SearchingSurface,
        errorMessage = null,
        cartQuantity = 0,
    )
}

@Preview(name = "AR — Error", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ArScreenPreviewError() {
    ArScreenPreviewScaffold(
        isSingleMode = false,
        placedModels = 2,
        isModelLoading = false,
        trackingStatus = ArTrackingStatus.Lost,
        errorMessage = "Не удалось загрузить модель",
        cartQuantity = 2,
    )
}

@Composable
private fun ArScreenPreviewScaffold(
    isSingleMode: Boolean,
    placedModels: Int,
    isModelLoading: Boolean,
    trackingStatus: ArTrackingStatus,
    errorMessage: String?,
    cartQuantity: Int,
) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
        ) {
            PreviewTopControls(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                isSingleMode = isSingleMode,
                placedModels = placedModels,
                isModelLoading = isModelLoading,
                trackingStatus = trackingStatus,
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                errorMessage?.let {
                    PreviewErrorBanner(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        message = it,
                    )
                }
                PreviewCartPicker(quantity = cartQuantity)
                PreviewGestureHint()
            }
        }
    }
}

@Composable
private fun PreviewTopControls(
    modifier: Modifier,
    isSingleMode: Boolean,
    placedModels: Int,
    isModelLoading: Boolean,
    trackingStatus: ArTrackingStatus,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                onClick = {},
                label = { Text(if (isSingleMode) "Режим: одна" else "Режим: несколько") },
            )
            AssistChip(
                onClick = {},
                enabled = placedModels > 0,
                label = { Text("Очистить") },
            )
        }

        if (isModelLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(160.dp),
            )
        }

        val trackingMessage = when (trackingStatus) {
            ArTrackingStatus.SearchingSurface -> "Ищем подходящую плоскость — перемещайте устройство."
            ArTrackingStatus.Lost -> "Трекинг потерян. Наведите камеру на освещенную поверхность."
            ArTrackingStatus.Tracking -> null
        }
        trackingMessage?.let {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        if (placedModels > 0) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = "Объектов в сцене: $placedModels",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun PreviewCartPicker(quantity: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (quantity > 0) {
            Button(onClick = {}) { Text("-") }
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = quantity.toString(),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            Button(onClick = {}) { Text("+") }
        } else {
            Button(onClick = {}) { Text("Добавить в корзину") }
        }
    }
}

@Composable
private fun PreviewGestureHint() {
    AssistChip(
        onClick = {},
        label = { Text("Тап — поставить • Long press — переместить • 2 пальца — вращать") },
    )
}

@Composable
private fun PreviewErrorBanner(
    modifier: Modifier,
    message: String,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Button(onClick = {}) { Text("Повторить") }
        }
    }
}
