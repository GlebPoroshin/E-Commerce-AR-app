package com.poroshin.rut.ar.common.ar.domain

import com.poroshin.rut.ar.common.ar.domain.result.ScaleResult
import com.poroshin.rut.ar.common.ar.domain.usecase.ScaleModelUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ScaleModelUseCaseTest {

    private val useCase = ScaleModelUseCase()
    private val defaultPolicy = ArScalePolicy(minScale = 0.01f, maxScale = 100f)

    @Test
    fun multipliesCurrentScaleByFactor() {
        val result = useCase(currentScale = 2.0f, factor = 1.5f, scalePolicy = defaultPolicy)

        assertIs<ScaleResult.Updated>(result)
        assertEquals(3.0f, (result as ScaleResult.Updated).newScale)
    }

    @Test
    fun clampsResultToMinScale() {
        val result = useCase(currentScale = 0.02f, factor = 0.1f, scalePolicy = defaultPolicy)

        assertIs<ScaleResult.Updated>(result)
        assertEquals(defaultPolicy.minScale, (result as ScaleResult.Updated).newScale)
    }

    @Test
    fun clampsResultToMaxScale() {
        val result = useCase(currentScale = 50f, factor = 10f, scalePolicy = defaultPolicy)

        assertIs<ScaleResult.Updated>(result)
        assertEquals(defaultPolicy.maxScale, (result as ScaleResult.Updated).newScale)
    }

    @Test
    fun rejectsNegativeCurrentScale() {
        val result = useCase(currentScale = -1f, factor = 1f, scalePolicy = defaultPolicy)

        assertIs<ScaleResult.Rejected>(result)
    }

    @Test
    fun rejectsZeroCurrentScale() {
        val result = useCase(currentScale = 0f, factor = 1f, scalePolicy = defaultPolicy)

        assertIs<ScaleResult.Rejected>(result)
    }

    @Test
    fun rejectsNaNFactor() {
        val result = useCase(currentScale = 1f, factor = Float.NaN, scalePolicy = defaultPolicy)

        assertIs<ScaleResult.Rejected>(result)
    }

    @Test
    fun rejectsZeroFactor() {
        val result = useCase(currentScale = 1f, factor = 0f, scalePolicy = defaultPolicy)

        assertIs<ScaleResult.Rejected>(result)
    }

    @Test
    fun dimensionOverloadProducesScaledResult() {
        val source = ArModelDimensionsMm(widthMm = 1000f, heightMm = 1000f, depthMm = 1000f)
        val target = ArModelDimensionsMm(widthMm = 500f, heightMm = 500f, depthMm = 500f)
        val result = useCase(
            sourceDimensions = source,
            targetDimensions = target,
            factor = 1.0f,
            scalePolicy = defaultPolicy,
        )

        assertIs<ScaleResult.Updated>(result)
        assertEquals(0.5f, (result as ScaleResult.Updated).newScale)
    }
}
