package com.poroshin.rut.ar.common.pdp.data.usecase

import com.poroshin.rut.ar.common.pdp.data.currentOs
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
        val currentVersion = modelRepository.checkExistingModel(sku)

        if (currentVersion == version) return@withContext

        modelRepository.downLoadModel(
            sku = sku,
            url = url,
            osType = currentOs(),
            onProgress = onProgress,
        )
    }
}
