package com.poroshin.rut.ar.common.pdp.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.poroshin.rut.ar.common.pdp.presentation.model.PdpState

@Composable
fun PdpScreen(
    state: PdpState,
    onModelLoadClick: (PdpState.Content) -> Unit,
    onDeleteModelClick: (PdpState.Content) -> Unit,
    onIncreaseCartClick: (PdpState.Content) -> Unit,
    onDecreaseCartClick: (PdpState.Content) -> Unit,
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
                onIncreaseCartClick = onIncreaseCartClick,
                onDecreaseCartClick = onDecreaseCartClick,
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
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PdpContent(
    state: PdpState.Content,
    onModelLoadClick: (PdpState.Content) -> Unit,
    onDeleteModelClick: (PdpState.Content) -> Unit,
    onIncreaseCartClick: (PdpState.Content) -> Unit,
    onDecreaseCartClick: (PdpState.Content) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        PdpHeaderImage(
            imageUrl = state.product.images.firstOrNull(),
            productName = state.product.name,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = state.product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "SKU ${state.product.sku}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = state.product.price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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
                        onClick = { onModelLoadClick(state) },
                    ) {
                        Text(text = "Посмотреть в AR")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onDeleteModelClick(state) },
                    ) {
                        Text(text = "Удалить модель")
                    }
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onModelLoadClick(state) },
                ) {
                    Text(text = "Скачать модель")
                }
            }

            if (state.cartQuantity > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onDecreaseCartClick(state) },
                    ) {
                        Text("<")
                    }
                    Text(
                        text = state.cartQuantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onIncreaseCartClick(state) },
                    ) {
                        Text(">")
                    }
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onIncreaseCartClick(state) },
                ) {
                    Text("Добавить в корзину")
                }
            }
        }
    }
}

@Composable
private fun PdpHeaderImage(
    imageUrl: String?,
    productName: String,
) {
    if (imageUrl.isNullOrBlank()) {
        ImagePlaceholder(text = "Нет изображения")
        return
    }

    val painter = rememberAsyncImagePainter(model = imageUrl)
    val painterState by painter.state.collectAsState()
    val intrinsicSize = painter.intrinsicSize
    val aspectRatio = if (
        intrinsicSize.isSpecified &&
        intrinsicSize.width > 0f &&
        intrinsicSize.height > 0f
    ) {
        intrinsicSize.width / intrinsicSize.height
    } else {
        4f / 3f
    }

    if (painterState is AsyncImagePainter.State.Error) {
        ImagePlaceholder(text = "Ошибка загрузки изображения")
        return
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = productName,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            contentScale = ContentScale.FillWidth,
        )

        if (painterState is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun ImagePlaceholder(
    text: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
