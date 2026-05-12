package com.poroshin.rut.ar.common.ar.domain.usecase

import com.poroshin.rut.ar.common.ar.domain.ArModelDimensionsMm
import com.poroshin.rut.ar.common.ar.domain.ArScaleCalculator
import com.poroshin.rut.ar.common.ar.domain.ArScalePolicy
import com.poroshin.rut.ar.common.ar.domain.result.ScaleResult

/**
 * Wraps [ArScaleCalculator] to compute a new uniform scale by applying [factor] to the
 * current scale and clamping it according to [scalePolicy].
 *
 * [sourceDimensions] — raw model dimensions in millimetres (native/geometry-space size).
 * [targetDimensions] — desired real-world dimensions in millimetres.
 * ArScaleCalculator.computeUniformScale produces the base scale from those dimensions;
 * then [factor] is applied on top and the result is clamped by [scalePolicy].
 *
 * If you already have a numeric [currentScale] and only want to apply a gesture delta,
 * pass null for both dimension params and use the [currentScale]/[factor]-only overload.
 */
class ScaleModelUseCase {

    /**
     * Multiply [currentScale] by [factor] and clamp to [scalePolicy].
     */
    operator fun invoke(
        currentScale: Float,
        factor: Float,
        scalePolicy: ArScalePolicy = ArScalePolicy.Default,
    ): ScaleResult {
        if (!currentScale.isFinite() || currentScale <= 0f) {
            return ScaleResult.Rejected("currentScale must be finite and > 0, got $currentScale")
        }
        if (!factor.isFinite() || factor <= 0f) {
            return ScaleResult.Rejected("factor must be finite and > 0, got $factor")
        }

        val candidate = currentScale * factor
        val clamped = scalePolicy.clamp(candidate)
        return ScaleResult.Updated(clamped)
    }

    /**
     * Derive a uniform scale from model geometry dimensions, apply [factor] on top,
     * then clamp to [scalePolicy].
     * Uses [ArScaleCalculator.computeUniformScale] internally.
     */
    operator fun invoke(
        sourceDimensions: ArModelDimensionsMm,
        targetDimensions: ArModelDimensionsMm,
        factor: Float,
        scalePolicy: ArScalePolicy = ArScalePolicy.Default,
    ): ScaleResult {
        if (!factor.isFinite() || factor <= 0f) {
            return ScaleResult.Rejected("factor must be finite and > 0, got $factor")
        }

        val sourceMeters = ArScaleCalculator.toMeters(sourceDimensions)
        val targetMeters = ArScaleCalculator.toMeters(targetDimensions)
        val baseScale = ArScaleCalculator.computeUniformScale(
            source = sourceMeters,
            target = targetMeters,
            policy = scalePolicy,
        )
        val candidate = baseScale * factor
        val clamped = scalePolicy.clamp(candidate)
        return ScaleResult.Updated(clamped)
    }
}
