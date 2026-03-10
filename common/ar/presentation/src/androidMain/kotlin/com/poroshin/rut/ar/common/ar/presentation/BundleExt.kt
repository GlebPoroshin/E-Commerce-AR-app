package com.poroshin.rut.ar.common.ar.presentation

import android.os.Bundle
import com.poroshin.rut.ar.common.ar.domain.ArObjectParams
import com.poroshin.rut.ar.common.cart.domain.CartItemSnapshot
import com.poroshin.rut.ar.common.pdp.domain.ArPlacement

private const val KEY_FILE_PATH = "ar.file_path"
private const val KEY_WIDTH = "ar.width"
private const val KEY_HEIGHT = "ar.height"
private const val KEY_DEPTH = "ar.depth"
private const val KEY_PLACEMENT = "ar.placement"
private const val KEY_CART_SKU = "ar.cart.sku"
private const val KEY_CART_NAME = "ar.cart.name"
private const val KEY_CART_PRICE = "ar.cart.price"
private const val KEY_CART_IMAGE = "ar.cart.image"

fun ArObjectParams.toBundle(): Bundle = Bundle().apply {
    putString(KEY_FILE_PATH, filePath)
    putFloat(KEY_WIDTH, widthMm)
    putFloat(KEY_HEIGHT, heightMm)
    putFloat(KEY_DEPTH, depthMm)
    putString(KEY_PLACEMENT, placement.name)
    cartItem?.let { snapshot ->
        putLong(KEY_CART_SKU, snapshot.sku)
        putString(KEY_CART_NAME, snapshot.name)
        putString(KEY_CART_PRICE, snapshot.priceText)
        putString(KEY_CART_IMAGE, snapshot.imageUrl)
    }
}

fun Bundle.toArObjectParams(): ArObjectParams? {
    val filePath = getString(KEY_FILE_PATH) ?: return null
    val placementName = getString(KEY_PLACEMENT) ?: return null
    if (!containsKey(KEY_WIDTH) || !containsKey(KEY_HEIGHT) || !containsKey(KEY_DEPTH)) {
        return null
    }

    val placement = try {
        ArPlacement.valueOf(placementName)
    } catch (_: IllegalArgumentException) {
        return null
    }

    val cartItem = if (containsKey(KEY_CART_SKU)) {
        val name = getString(KEY_CART_NAME)
        val price = getString(KEY_CART_PRICE)
        val imageUrl = getString(KEY_CART_IMAGE)
        if (name != null && price != null && imageUrl != null) {
            CartItemSnapshot(
                sku = getLong(KEY_CART_SKU),
                name = name,
                priceText = price,
                imageUrl = imageUrl,
            )
        } else {
            null
        }
    } else {
        null
    }

    return ArObjectParams(
        filePath = filePath,
        widthMm = getFloat(KEY_WIDTH),
        heightMm = getFloat(KEY_HEIGHT),
        depthMm = getFloat(KEY_DEPTH),
        placement = placement,
        cartItem = cartItem,
    )
}
