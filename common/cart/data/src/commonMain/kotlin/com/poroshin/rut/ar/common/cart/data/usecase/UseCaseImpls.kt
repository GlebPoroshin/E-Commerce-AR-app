package com.poroshin.rut.ar.common.cart.data.usecase

import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.cart.domain.repository.CartRepository
import com.poroshin.rut.ar.common.cart.domain.usecase.AddOrIncrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.DecrementCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartBadgeCountUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartLinesUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveCartSummaryUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.ObserveSkuQuantityUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.RemoveCartItemUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.RunLegacyModelVersionsMigrationUseCase
import com.poroshin.rut.ar.common.cart.domain.usecase.SetCartItemQuantityUseCase

class ObserveCartLinesUseCaseImpl(
    private val repository: CartRepository,
) : ObserveCartLinesUseCase {
    override fun invoke() = repository.observeLines()
}

class ObserveCartBadgeCountUseCaseImpl(
    private val repository: CartRepository,
) : ObserveCartBadgeCountUseCase {
    override fun invoke() = repository.observeUniqueSkuCount()
}

class ObserveSkuQuantityUseCaseImpl(
    private val repository: CartRepository,
) : ObserveSkuQuantityUseCase {
    override fun invoke(sku: Long) = repository.observeSkuQuantity(sku)
}

class ObserveCartSummaryUseCaseImpl(
    private val repository: CartRepository,
) : ObserveCartSummaryUseCase {
    override fun invoke() = repository.observeSummary()
}

class AddOrIncrementCartItemUseCaseImpl(
    private val repository: CartRepository,
) : AddOrIncrementCartItemUseCase {
    override suspend fun invoke(snapshot: CartItemSnapshot) {
        repository.addOrIncrement(snapshot)
    }
}

class DecrementCartItemUseCaseImpl(
    private val repository: CartRepository,
) : DecrementCartItemUseCase {
    override suspend fun invoke(sku: Long) {
        repository.decrement(sku)
    }
}

class SetCartItemQuantityUseCaseImpl(
    private val repository: CartRepository,
) : SetCartItemQuantityUseCase {
    override suspend fun invoke(sku: Long, quantity: Int) {
        repository.setQuantity(sku, quantity)
    }
}

class RemoveCartItemUseCaseImpl(
    private val repository: CartRepository,
) : RemoveCartItemUseCase {
    override suspend fun invoke(sku: Long) {
        repository.remove(sku)
    }
}

class RunLegacyModelVersionsMigrationUseCaseImpl(
    private val repository: CartRepository,
) : RunLegacyModelVersionsMigrationUseCase {
    override suspend fun invoke() {
        repository.runLegacyModelVersionMigration()
    }
}
