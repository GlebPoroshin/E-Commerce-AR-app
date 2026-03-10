package com.poroshin.rut.ar.common.pdp.domain.usecase

fun interface DeleteProductModelUseCase {
    suspend operator fun invoke(sku: Long)
}
