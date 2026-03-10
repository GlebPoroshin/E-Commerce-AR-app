package com.poroshin.rut.ar.common.cart.domain

import kotlinx.datetime.Instant

data class CartItemSnapshot(
    val sku: Long,
    val name: String,
    val priceText: String,
    val imageUrl: String,
)

data class CartLine(
    val snapshot: CartItemSnapshot,
    val quantity: Int,
    val updatedAt: Instant,
)

data class CartSummary(
    val totalAmountRubles: Long,
    val hasInvalidPriceItems: Boolean,
    val invalidItemsCount: Int,
)

object CartBadgeFormatter {
    fun format(count: Int): String? {
        return when {
            count <= 0 -> null
            count > 99 -> "99+"
            else -> count.toString()
        }
    }
}

object CartPriceParser {
    fun parseRubles(priceText: String): Long? {
        val normalized = buildString {
            priceText.forEach { ch ->
                if (ch.isDigit()) append(ch)
            }
        }

        return normalized.toLongOrNull()
    }
}

object CartPriceFormatter {
    fun formatRubles(value: Long): String {
        val raw = value.coerceAtLeast(0).toString()
        val withSpaces = raw.reversed().chunked(3).joinToString(" ").reversed()
        return "$withSpaces ₽"
    }
}
