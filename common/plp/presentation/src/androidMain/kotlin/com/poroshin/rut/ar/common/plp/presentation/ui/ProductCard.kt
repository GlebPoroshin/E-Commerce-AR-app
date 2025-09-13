package com.poroshin.rut.ar.common.plp.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.poroshin.rut.ar.common.plp.domain.Product
import kotlin.math.round

@Composable
fun ProductCard(
	product: Product,
	onProductClick: (Long) -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.background(MaterialTheme.colorScheme.surface)
			.clickable { onProductClick(product.sku) },
		horizontalAlignment = Alignment.Start,
		verticalArrangement = Arrangement.Top,
	) {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(product.imageUrl)
				.crossfade(true)
				.build(),
			contentDescription = product.name,
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1f),
			contentScale = ContentScale.FillWidth,
		)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp, vertical = 10.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp),
		) {
			Text(
				text = product.name,
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
			)
			Spacer(Modifier.height(2.dp))
			Column(
				verticalArrangement = Arrangement.spacedBy(2.dp)
			) {
				Text(
					text = "${product.price} ₽",
					style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
				)
				product.oldPrice?.let { old ->
					Text(
						text = "$old ₽",
						style = MaterialTheme.typography.bodySmall.copy(
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							textDecoration = TextDecoration.LineThrough,
						),
						maxLines = 1,
						overflow = TextOverflow.Clip,
					)
				}
			}
			if (product.rate > 0.0) {
				val rounded = round(product.rate * 10.0) / 10.0
				Text(
					text = "★ $rounded",
					style = MaterialTheme.typography.bodySmall.copy(
						color = MaterialTheme.colorScheme.primary,
						fontWeight = FontWeight.Medium,
					)
				)
			}
		}
	}
}

@Preview
@Composable
private fun ProductCardPreview() {
	MaterialTheme {
		ProductCard(
			product = Product(
				sku = 1000L,
				name = "Диван",
				description = "Самая лучшая наша модель",
				price = "18 000",
				imageUrl = "https://picsum.photos/seed/sofa/800/800",
				oldPrice = "24 990",
				discount = 28,
				rate = 4.6
			),
			onProductClick = {/* no-op */ }
		)
	}
}
