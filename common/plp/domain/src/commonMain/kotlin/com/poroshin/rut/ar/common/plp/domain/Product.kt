package com.poroshin.rut.ar.common.plp.domain

data class Product(
    val sku: Long,
    val name: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val oldPrice: String? = null,
    val discount: Int? = null,
    val rate: Double = 0.0,
)
