package com.poroshin.rut.ar.common.ar.domain

import com.poroshin.rut.ar.common.ar.domain.result.RotationResult
import com.poroshin.rut.ar.common.ar.domain.usecase.RotateModelUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RotateModelUseCaseTest {

    private val useCase = RotateModelUseCase()

    @Test
    fun returnsUpdatedWhenDeltaIsValid() {
        val current = floatArrayOf(0f, 90f, 0f)
        val result = useCase(current, 30f)

        assertIs<RotationResult.Updated>(result)
        assertEquals(120f, (result as RotationResult.Updated).newRotation[1])
    }

    @Test
    fun normalizesAngleAbove360() {
        val current = floatArrayOf(0f, 350f, 0f)
        val result = useCase(current, 30f)

        assertIs<RotationResult.Updated>(result)
        assertEquals(20f, (result as RotationResult.Updated).newRotation[1])
    }

    @Test
    fun normalizesNegativeDelta() {
        val current = floatArrayOf(0f, 10f, 0f)
        val result = useCase(current, -30f)

        assertIs<RotationResult.Updated>(result)
        assertEquals(340f, (result as RotationResult.Updated).newRotation[1])
    }

    @Test
    fun doesNotMutateOriginalArray() {
        val current = floatArrayOf(0f, 45f, 0f)
        useCase(current, 15f)

        assertEquals(45f, current[1], "Original array must not be mutated")
    }

    @Test
    fun rejectsNaNDelta() {
        val current = floatArrayOf(0f, 0f, 0f)
        val result = useCase(current, Float.NaN)

        assertIs<RotationResult.Rejected>(result)
    }

    @Test
    fun rejectsInfiniteDelta() {
        val current = floatArrayOf(0f, 0f, 0f)
        val result = useCase(current, Float.POSITIVE_INFINITY)

        assertIs<RotationResult.Rejected>(result)
    }

    @Test
    fun rejectsTooShortArray() {
        val current = floatArrayOf(0f, 45f)
        val result = useCase(current, 10f)

        assertIs<RotationResult.Rejected>(result)
    }
}
