package com.poroshin.rut.ar.common.cart.domain.repository

import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.cart.domain.CartLine
import com.poroshin.rut.ar.common.cart.domain.CartSummary
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun observeLines(): Flow<List<CartLine>>

    fun observeUniqueSkuCount(): Flow<Int>

    fun observeSkuQuantity(sku: Long): Flow<Int>

    fun observeSummary(): Flow<CartSummary>

    suspend fun addOrIncrement(snapshot: CartItemSnapshot)

    suspend fun decrement(sku: Long)

    suspend fun setQuantity(sku: Long, quantity: Int)

    suspend fun remove(sku: Long)

    suspend fun getModelVersion(sku: Long): Int?

    suspend fun saveModelVersion(sku: Long, version: Int)

    suspend fun deleteModelVersion(sku: Long)

    suspend fun runLegacyModelVersionMigration()
}
