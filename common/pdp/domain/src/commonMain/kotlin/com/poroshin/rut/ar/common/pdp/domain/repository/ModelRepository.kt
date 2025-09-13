package com.poroshin.rut.ar.common.pdp.domain.repository

import com.poroshin.rut.ar.common.pdp.domain.OsType

interface ModelRepository {

    suspend fun checkExistingModel(sku: Long): Int?

    suspend fun downLoadModel(
        sku: Long,
        url: String,
        osType: OsType,
        onProgress: (received: Long, total: Long?) -> Unit,
    )
}
