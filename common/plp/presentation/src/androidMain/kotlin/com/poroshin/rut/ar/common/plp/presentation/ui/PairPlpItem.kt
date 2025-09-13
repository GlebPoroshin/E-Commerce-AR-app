package com.poroshin.rut.ar.common.plp.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poroshin.rut.ar.common.plp.domain.Product

@Composable
fun PairPlpItems(
    left: Product,
    right: Product?,
    onItemClick: (sku: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductCard(
                product = left,
                onProductClick = onItemClick,
                modifier = Modifier
                    .weight(if (right != null) 1f else 0.5f)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(4.dp),
            )
            if (right != null) {
                VerticalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                ProductCard(
                    product = right,
                    onProductClick = onItemClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(4.dp),
                )
            }
        }
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun PairPlpItemsShimmer(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shimmer(cornerRadius = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(18.dp)
                        .shimmer(cornerRadius = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .shimmer(cornerRadius = 4.dp)
                )
            }

            VerticalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shimmer(cornerRadius = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(18.dp)
                        .shimmer(cornerRadius = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .shimmer(cornerRadius = 4.dp)
                )
            }
        }
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
