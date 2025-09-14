package com.poroshin.rut.ar.common.pdp.domain

/**
 * Доп. инфо для AR. Поле может отсутствовать (null) в продукте.
 */
data class ArInfo(
    val version: Int?,
    val arType: ArType,
    val arRecourceUrl: String,
    val width: Int, // В миллиметрах
    val height: Int,
    val depth: Int? = null
)

enum class ArType { OBJECT, FLOOR, WALL }

enum class OsType { ANDROID, IOS }

/**
 * Расширенная модель продукта для PDP.
 * Содержит больше информации, чем PLP Product.
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
 * Параметры для получения PDP.
 */
data class GetPdpParams(val sku: Long)

class ModelLoadException(override val message: String): Exception()
