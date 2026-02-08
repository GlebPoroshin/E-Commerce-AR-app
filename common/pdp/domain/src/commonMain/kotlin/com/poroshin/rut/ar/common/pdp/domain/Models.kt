package com.poroshin.rut.ar.common.pdp.domain

/**
 * Additional info for AR. Field may be absent (null) in product.
 */
data class ArInfo(
    val version: Int?,
    val arType: ArType,
    val placement: ArPlacement,
    val arResourceUrl: String,
    val width: Float, // In millimeters
    val height: Float,
    val depth: Float? = null
)

enum class ArType { OBJECT, FLOOR, WALL }

enum class ArPlacement {
    FLOOR,
    CEILING,
    ANY_HORIZONTAL,
    ANY_VERTICAL,
    ANY_SURFACE
}

enum class OsType { ANDROID, IOS }

/**
 * Extended product model for PDP.
 * Contains more information than PLP Product.
 */
data class ProductPageInfo(
    val sku: Long,
    val name: String,
    val description: String,
    val price: String,
    val images: List<String>,
    val oldPrice: String? = null,
    val discount: Int? = null,
    val rating: Double = 0.0,
    val characteristics: Map<String, String> = emptyMap(),
    val stock: Int? = null,
    val deliveryInfo: String? = null,
    val ar: ArInfo? = null,
)

/**
 * Parameters for getting PDP.
 */
data class GetPdpParams(val sku: Long)

class ModelLoadException(override val message: String): Exception()
