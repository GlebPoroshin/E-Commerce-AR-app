package com.poroshin.rut.ar.common.pdp.domain.usecase

fun interface CheckModelExistsUseCase {
    suspend operator fun invoke(sku: Long): Boolean
}
