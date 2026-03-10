package com.poroshin.rut.ar.common.cart.domain.usecase

import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.cart.domain.CartLine
import com.poroshin.rut.ar.common.cart.domain.CartSummary
import kotlinx.coroutines.flow.Flow

fun interface ObserveCartLinesUseCase {
    operator fun invoke(): Flow<List<CartLine>>
}

fun interface ObserveCartBadgeCountUseCase {
    operator fun invoke(): Flow<Int>
}

fun interface ObserveSkuQuantityUseCase {
    operator fun invoke(sku: Long): Flow<Int>
}

fun interface ObserveCartSummaryUseCase {
    operator fun invoke(): Flow<CartSummary>
}

fun interface AddOrIncrementCartItemUseCase {
    suspend operator fun invoke(snapshot: CartItemSnapshot)
}

fun interface DecrementCartItemUseCase {
    suspend operator fun invoke(sku: Long)
}

fun interface SetCartItemQuantityUseCase {
    suspend operator fun invoke(sku: Long, quantity: Int)
}

fun interface RemoveCartItemUseCase {
    suspend operator fun invoke(sku: Long)
}

fun interface RunLegacyModelVersionsMigrationUseCase {
    suspend operator fun invoke()
}
