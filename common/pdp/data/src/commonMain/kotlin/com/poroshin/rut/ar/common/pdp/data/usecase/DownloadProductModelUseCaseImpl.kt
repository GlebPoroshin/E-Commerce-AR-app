package com.poroshin.rut.ar.common.pdp.data.usecase

import com.poroshin.rut.ar.common.pdp.data.getDownloadPath
import com.poroshin.rut.ar.common.pdp.data.objectModelType
import com.poroshin.rut.ar.common.pdp.domain.ModelLoadException
import com.poroshin.rut.ar.common.pdp.domain.repository.ModelRepository
import com.poroshin.rut.ar.common.pdp.domain.usecase.DownloadProductModelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DownloadProductModelUseCaseImpl(
    private val modelRepository: ModelRepository,
) : DownloadProductModelUseCase {
    override suspend fun invoke(
        sku: Long,
        url: String,
        version: Int,
        onProgress: (received: Long, total: Long?) -> Unit,
    ) = withContext(Dispatchers.IO) {
        try {
            val currentVersion = modelRepository.checkExistingModel(sku)

            if (currentVersion == version) return@withContext

            val objectModelType = objectModelType()
            val path = getDownloadPath( "ar/models/${sku}${objectModelType}")

            modelRepository.downLoadModel(
                sku = sku,
                url = url,
                path = path,
                onProgress = onProgress,
            )

            modelRepository.saveModelVersion(sku, version)
        } catch (e: Exception) {
            throw ModelLoadException(e.message ?: "Произошла ошикбка : $e")
        }
    }
}
