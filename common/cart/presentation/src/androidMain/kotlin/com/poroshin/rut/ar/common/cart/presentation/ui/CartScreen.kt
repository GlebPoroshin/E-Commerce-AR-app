package com.poroshin.rut.ar.common.cart.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.cart.domain.CartPriceFormatter
import com.poroshin.rut.ar.common.cart.presentation.model.CartState

@Composable
fun CartScreen(
    state: CartState,
    onIncrease: (CartItemSnapshot) -> Unit,
    onDecrease: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onItemClick: (Long) -> Unit,
    onMainClick: () -> Unit,
) {
    when (state) {
        CartState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is CartState.Content -> {
            if (state.lines.isEmpty()) {
                EmptyCart(onMainClick = onMainClick)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.lines, key = { line -> line.snapshot.sku }) { line ->
                            CartLineItem(
                                snapshot = line.snapshot,
                                quantity = line.quantity,
                                onIncrease = onIncrease,
                                onDecrease = onDecrease,
                                onRemove = onRemove,
                                onItemClick = onItemClick,
                            )
                        }
                    }

                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Итого: ${CartPriceFormatter.formatRubles(state.summary.totalAmountRubles)}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (state.summary.hasInvalidPriceItems) {
                                Text(
                                    text = "Некорректные цены: ${state.summary.invalidItemsCount}. Эти позиции считаются как 0.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCart(
    onMainClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Корзина пуста",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onMainClick) {
            Text("Перейти на главную")
        }
    }
}

@Composable
private fun CartLineItem(
    snapshot: CartItemSnapshot,
    quantity: Int,
    onIncrease: (CartItemSnapshot) -> Unit,
    onDecrease: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onItemClick: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(snapshot.sku) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = snapshot.imageUrl,
            contentDescription = snapshot.name,
            modifier = Modifier
                .size(72.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = snapshot.name,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = snapshot.priceText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { onDecrease(snapshot.sku) }) {
                    Text("-")
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedButton(onClick = { onIncrease(snapshot) }) {
                    Text("+")
                }
            }
        }

        OutlinedButton(onClick = { onRemove(snapshot.sku) }) {
            Text("Удалить")
        }
    }
}
