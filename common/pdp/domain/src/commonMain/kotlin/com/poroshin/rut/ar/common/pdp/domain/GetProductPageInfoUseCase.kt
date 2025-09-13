package com.poroshin.rut.ar.common.pdp.domain

fun interface GetProductPageInfoUseCase {
    suspend operator fun invoke(params: GetPdpParams): ProductPageInfo
}


