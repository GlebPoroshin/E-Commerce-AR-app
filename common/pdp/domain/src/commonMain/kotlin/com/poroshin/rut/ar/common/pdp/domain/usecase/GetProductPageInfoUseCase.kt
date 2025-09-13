package com.poroshin.rut.ar.common.pdp.domain.usecase

import com.poroshin.rut.ar.common.pdp.domain.GetPdpParams
import com.poroshin.rut.ar.common.pdp.domain.ProductPageInfo

fun interface GetProductPageInfoUseCase {
    suspend operator fun invoke(params: GetPdpParams): ProductPageInfo
}


