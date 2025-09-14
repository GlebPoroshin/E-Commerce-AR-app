package com.poroshin.rut.ar.common.pdp.data.usecase

import com.poroshin.rut.ar.common.pdp.data.getDownloadPath
import com.poroshin.rut.ar.common.pdp.data.objectModelType
import com.poroshin.rut.ar.common.pdp.domain.ModelLoadException
import com.poroshin.rut.ar.common.pdp.domain.repository.ModelRepository
import com.poroshin.rut.ar.common.pdp.domain.usecase.DownloadProductModelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path

class DownloadProductModelUseCaseImpl(
    private val modelRepository: ModelRepository,
) : DownloadProductModelUseCase {

    override suspend fun invoke(
        sku: Long,
        url: String,
        version: Int,
        onProgress: (received: Long, total: Long?) -> Unit,
    ): Path = withContext(Dispatchers.IO) {
        try {
            val objectModelType = objectModelType()
            val path = getDownloadPath("ar/models/${sku}${objectModelType}")
            val currentVersion = modelRepository.checkExistingModel(sku)

            if (currentVersion != version) {
                modelRepository.downLoadModel(sku, path, url, onProgress)
                modelRepository.saveModelVersion(sku, version)
            }

            path
        } catch (e: Exception) {
            throw ModelLoadException(e.message ?: "Произошла ошикбка : $e")
        }
    }
}
