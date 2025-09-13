package com.poroshin.rut.ar.common.pdp.domain

import com.poroshin.rut.ar.common.pdp.domain.OsType

interface ModelRepository {

    suspend fun checkExistingModel(sku: Long): Boolean

    suspend fun downLoadModel(sku: Long, osType: OsType): Boolean
}
