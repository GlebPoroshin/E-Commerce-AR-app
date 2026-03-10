package com.poroshin.rut.ar.common.ar.domain

import com.poroshin.rut.ar.common.pdp.domain.ArPlacement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArDomainPoliciesTest {

    @Test
    fun distancePolicyClampsWithinRange() {
        val policy = ArDistancePolicy(minDistanceMeters = 0.35f, maxDistanceMeters = 4.0f)

        assertEquals(0.35f, policy.clamp(0.1f))
        assertEquals(1.5f, policy.clamp(1.5f))
        assertEquals(4.0f, policy.clamp(10f))
    }

    @Test
    fun scaleCalculatorUsesSafeModelDimensionsAndPolicyClamp() {
        val target = ArScaleCalculator.toMeters(
            ArModelDimensionsMm(widthMm = 500f, heightMm = 400f, depthMm = 300f),
        )
        val source = ArModelDimensionsMeters(width = 0f, height = 0f, depth = 0f)
        val policy = ArScalePolicy(minScale = 0.01f, maxScale = 100f)

        val scale = ArScaleCalculator.computeUniformScale(
            source = source,
            target = target,
            policy = policy,
        )

        assertEquals(100f, scale)
    }

    @Test
    fun placementPolicyMatchesExpectedPlanes() {
        assertTrue(ArPlacementPolicy.supportsPlane(ArPlacement.ANY_SURFACE, ArPlaneType.Vertical))
        assertTrue(ArPlacementPolicy.supportsPlane(ArPlacement.ANY_HORIZONTAL, ArPlaneType.HorizontalUpward))
        assertFalse(ArPlacementPolicy.supportsPlane(ArPlacement.ANY_HORIZONTAL, ArPlaneType.Vertical))
        assertTrue(ArPlacementPolicy.supportsPlane(ArPlacement.FLOOR, ArPlaneType.HorizontalUpward))
        assertFalse(ArPlacementPolicy.supportsPlane(ArPlacement.FLOOR, ArPlaneType.HorizontalDownward))
        assertTrue(ArPlacementPolicy.supportsPlane(ArPlacement.CEILING, ArPlaneType.HorizontalDownward))
    }

    @Test
    fun dimensionValidatorRejectsInvalidData() {
        assertTrue(
            ArDimensionValidator.validate(
                ArModelDimensionsMm(widthMm = 1f, heightMm = 1f, depthMm = 1f),
            ) is ArValidationResult.Valid,
        )
        assertTrue(
            ArDimensionValidator.validate(
                ArModelDimensionsMm(widthMm = 0.9f, heightMm = 1f, depthMm = 1f),
            ) is ArValidationResult.Invalid,
        )
        assertTrue(
            ArDimensionValidator.validate(
                ArModelDimensionsMm(widthMm = Float.NaN, heightMm = 1f, depthMm = 1f),
            ) is ArValidationResult.Invalid,
        )
    }

    @Test
    fun collisionEvaluatorDetectsOverlap() {
        val a = ArAabb(
            center = ArVector3(0f, 0f, 0f),
            halfExtents = ArVector3(1f, 1f, 1f),
        )
        val b = ArAabb(
            center = ArVector3(1f, 0f, 0f),
            halfExtents = ArVector3(1f, 1f, 1f),
        )
        val c = ArAabb(
            center = ArVector3(5f, 0f, 0f),
            halfExtents = ArVector3(1f, 1f, 1f),
        )

        assertTrue(ArCollisionEvaluator.intersects(a, b))
        assertFalse(ArCollisionEvaluator.intersects(a, c))
    }
}
