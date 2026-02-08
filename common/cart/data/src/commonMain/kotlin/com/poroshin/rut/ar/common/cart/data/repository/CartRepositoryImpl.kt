package com.poroshin.rut.ar.common.cart.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.poroshin.rut.ar.common.cart.data.db.CartDatabase
import com.poroshin.rut.ar.common.cart.data.db.Cart_items
import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.cart.domain.CartLine
import com.poroshin.rut.ar.common.cart.domain.CartPriceParser
import com.poroshin.rut.ar.common.cart.domain.CartSummary
import com.poroshin.rut.ar.common.cart.domain.repository.CartRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class CartRepositoryImpl(
    private val database: CartDatabase,
    private val settings: Settings,
) : CartRepository {

    private val queries = database.cartDatabaseQueries

    override fun observeLines(): Flow<List<CartLine>> {
        return queries.selectCartItems()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeUniqueSkuCount(): Flow<Int> {
        return queries.selectCartItemCount()
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .map { it.toInt() }
    }

    override fun observeSkuQuantity(sku: Long): Flow<Int> {
        return queries.selectCartItemBySku(sku)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.quantity?.toInt() ?: 0 }
    }

    override fun observeSummary(): Flow<CartSummary> {
        return observeLines().map { lines ->
            var totalAmount = 0L
            var invalidItemsCount = 0

            lines.forEach { line ->
                val parsedPrice = CartPriceParser.parseRubles(line.snapshot.priceText)
                if (parsedPrice == null) {
                    invalidItemsCount++
                } else {
                    totalAmount += parsedPrice * line.quantity
                }
            }

            CartSummary(
                totalAmountRubles = totalAmount,
                hasInvalidPriceItems = invalidItemsCount > 0,
                invalidItemsCount = invalidItemsCount,
            )
        }
    }

    override suspend fun addOrIncrement(snapshot: CartItemSnapshot) {
        withContext(Dispatchers.IO) {
            val now = nowEpochMs()
            val existing = queries.selectCartItemBySku(snapshot.sku).executeAsOneOrNull()
            if (existing == null) {
                queries.upsertIncrementCartItem(
                    sku = snapshot.sku,
                    name = snapshot.name,
                    price_text = snapshot.priceText,
                    image_url = snapshot.imageUrl,
                    updated_at_epoch_ms = now,
                )
            } else {
                queries.incrementCartItem(
                    name = snapshot.name,
                    price_text = snapshot.priceText,
                    image_url = snapshot.imageUrl,
                    updated_at_epoch_ms = now,
                    sku = snapshot.sku,
                )
            }
        }
    }

    override suspend fun decrement(sku: Long) = withContext(Dispatchers.IO) {
        val row = queries.selectCartItemBySku(sku).executeAsOneOrNull() ?: return@withContext

        val newQuantity = row.quantity.toInt() - 1
        if (newQuantity <= 0) {
            queries.deleteCartItem(sku)
        } else {
            queries.updateCartItemQuantity(
                quantity = newQuantity.toLong(),
                updated_at_epoch_ms = nowEpochMs(),
                sku = sku,
            )
        }
    }

    override suspend fun setQuantity(sku: Long, quantity: Int) {
        withContext(Dispatchers.IO) {
            if (quantity <= 0) {
                queries.deleteCartItem(sku)
            } else {
                queries.updateCartItemQuantity(
                    quantity = quantity.toLong(),
                    updated_at_epoch_ms = nowEpochMs(),
                    sku = sku,
                )
            }
        }
    }

    override suspend fun remove(sku: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteCartItem(sku)
        }
    }

    override suspend fun getModelVersion(sku: Long): Int? = withContext(Dispatchers.IO) {
        queries.selectModelVersion(sku).executeAsOneOrNull()?.toInt()
    }

    override suspend fun saveModelVersion(sku: Long, version: Int) {
        withContext(Dispatchers.IO) {
            queries.upsertModelVersion(
                sku = sku,
                version = version.toLong(),
                updated_at_epoch_ms = nowEpochMs(),
            )
        }
    }

    override suspend fun deleteModelVersion(sku: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteModelVersion(sku)
        }
    }

    override suspend fun runLegacyModelVersionMigration() = withContext(Dispatchers.IO) {
        if (queries.selectMetaValue(META_MIGRATION_KEY).executeAsOneOrNull() == MIGRATION_DONE_VALUE) {
            return@withContext
        }

        val modelKeys = settings.keys.filter { it.startsWith(LEGACY_MODEL_PREFIX) }

        database.transaction {
            modelKeys.forEach { key ->
                val sku = key.removePrefix(LEGACY_MODEL_PREFIX).toLongOrNull() ?: return@forEach
                val version = settings.getIntOrNull(key) ?: return@forEach
                val existingVersion = queries.selectModelVersion(sku).executeAsOneOrNull()?.toInt()

                if (existingVersion == null || existingVersion < version) {
                    queries.upsertModelVersion(
                        sku = sku,
                        version = version.toLong(),
                        updated_at_epoch_ms = nowEpochMs(),
                    )
                }
            }

            queries.upsertMetaValue(
                key = META_MIGRATION_KEY,
                value_ = MIGRATION_DONE_VALUE,
            )
        }

        modelKeys.forEach { settings.remove(it) }
    }

    private fun nowEpochMs(): Long = Clock.System.now().toEpochMilliseconds()

    private fun Cart_items.toDomain(): CartLine {
        return CartLine(
            snapshot = CartItemSnapshot(
                sku = sku,
                name = name,
                priceText = price_text,
                imageUrl = image_url,
            ),
            quantity = quantity.toInt(),
            updatedAt = Instant.fromEpochMilliseconds(updated_at_epoch_ms),
        )
    }

    private companion object {
        const val LEGACY_MODEL_PREFIX = "model_"
        const val META_MIGRATION_KEY = "legacy_model_versions_migrated"
        const val MIGRATION_DONE_VALUE = "1"
    }
}
