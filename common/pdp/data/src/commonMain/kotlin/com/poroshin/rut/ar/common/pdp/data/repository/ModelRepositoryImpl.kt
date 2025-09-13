package com.poroshin.rut.ar.common.pdp.data.repository

import com.poroshin.rut.ar.common.pdp.data.datasource.LocalModelDataSource
import com.poroshin.rut.ar.common.pdp.data.datasource.RemoteModelDataSource
import com.poroshin.rut.ar.common.pdp.domain.repository.ModelRepository
import kotlinx.io.files.Path

class ModelRepositoryImpl(
    private val localModelDataSource: LocalModelDataSource,
    private val remoteModelDataSource: RemoteModelDataSource,
) : ModelRepository {

    override suspend fun checkExistingModel(sku: Long): Int? {
        return localModelDataSource.checkModelVersion(sku)
    }

    override suspend fun saveModelVersion(sku: Long, version: Int) {
        localModelDataSource.saveModelVersion(sku, version)
    }

    override suspend fun downLoadModel(
        sku: Long,
        path: Path,
        url: String,
        onProgress: (received: Long, total: Long?) -> Unit,
    ) {
        remoteModelDataSource.downloadFile(
            url = url,
            dest = path,
            onProgress = onProgress,
        )
    }
}
