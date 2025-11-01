package com.poroshin.rut.ar.common.pdp.data.usecase

import com.poroshin.rut.ar.common.pdp.data.getDownloadPath
import com.poroshin.rut.ar.common.pdp.data.objectModelType
import com.poroshin.rut.ar.common.pdp.domain.repository.ModelRepository
import com.poroshin.rut.ar.common.pdp.domain.usecase.CheckModelExistsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class CheckModelExistsUseCaseImpl(
    private val modelRepository: ModelRepository,
) : CheckModelExistsUseCase {

    override suspend fun invoke(sku: Long): Boolean = withContext(Dispatchers.IO) {
        val version = modelRepository.checkExistingModel(sku)
        if (version == null) {
            return@withContext false
        }

        val objectModelType = objectModelType()
        val path = getDownloadPath("ar/models/${sku}${objectModelType}")
        modelRepository.isModelFileExists(path)
    }
}
