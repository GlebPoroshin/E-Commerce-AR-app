package com.poroshin.rut.ar.common.pdp.domain.usecase

interface DownloadProductModelUseCase {
    suspend operator fun invoke(
        sku: Long,
        url: String,
        version: Int,
        onProgress: (received: Long, total: Long?) -> Unit,
    )
}
