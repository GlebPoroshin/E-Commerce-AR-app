package com.poroshin.rut.ar.common.pdp.data.repository

import com.poroshin.rut.ar.common.pdp.data.datasource.LocalModelDataSource
import com.poroshin.rut.ar.common.pdp.data.datasource.RemoteModelDataSource
import com.poroshin.rut.ar.common.pdp.data.getDownloadPath
import com.poroshin.rut.ar.common.pdp.data.objectModelType
import com.poroshin.rut.ar.common.pdp.domain.ModelLoadException
import com.poroshin.rut.ar.common.pdp.domain.repository.ModelRepository
import com.poroshin.rut.ar.common.pdp.domain.OsType

class ModelRepositoryImpl(
    private val localModelDataSource: LocalModelDataSource,
    private val remoteModelDataSource: RemoteModelDataSource,
) : ModelRepository {

    override suspend fun checkExistingModel(sku: Long): Int? {
        return localModelDataSource.checkModelVersion(sku)
    }

    override suspend fun downLoadModel(
        sku: Long,
        url: String,
        osType: OsType,
        onProgress: (received: Long, total: Long?) -> Unit,
    ) {
        val objectModelType = objectModelType()
        val path = getDownloadPath(sku.toString() + objectModelType)

        try {
            remoteModelDataSource.downloadFile(
                url = url,
                dest = path,
                onProgress = onProgress,
            )
        } catch (e: Exception) {
            throw ModelLoadException(e.message ?: "Произошла ошикбка : $e")
        }
    }
}
