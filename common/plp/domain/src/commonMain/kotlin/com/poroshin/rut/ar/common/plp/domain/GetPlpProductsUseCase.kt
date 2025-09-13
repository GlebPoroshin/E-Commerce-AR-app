package com.poroshin.rut.ar.common.plp.domain

fun interface GetPlpProductsUseCase {
    suspend operator fun invoke(page: Int?): List<Product>
}


