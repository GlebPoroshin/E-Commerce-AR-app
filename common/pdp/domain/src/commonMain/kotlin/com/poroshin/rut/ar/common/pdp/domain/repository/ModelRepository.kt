package com.poroshin.rut.ar.common.pdp.domain.repository

import kotlinx.io.files.Path

interface ModelRepository {

    suspend fun checkExistingModel(sku: Long): Int?

    suspend fun saveModelVersion(sku: Long, version: Int)

    suspend fun downLoadModel(
        sku: Long,
        path: Path,
        url: String,
        onProgress: (received: Long, total: Long?) -> Unit,
    )
}
