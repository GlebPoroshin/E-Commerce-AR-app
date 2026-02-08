package com.poroshin.rut.ar.common.ar.domain

data class ArModelDimensionsMm(
    val widthMm: Float,
    val heightMm: Float,
    val depthMm: Float,
)

data class ArModelDimensionsMeters(
    val width: Float,
    val height: Float,
    val depth: Float,
)

object ArScaleCalculator {
    private const val MinDimensionMm = 1f
    private const val MillimetersInMeter = 1000f
    private const val MinSourceDimensionMeters = 1e-5f

    fun safeDimensionsMm(raw: ArModelDimensionsMm): ArModelDimensionsMm {
        return ArModelDimensionsMm(
            widthMm = raw.widthMm.coerceAtLeast(MinDimensionMm),
            heightMm = raw.heightMm.coerceAtLeast(MinDimensionMm),
            depthMm = raw.depthMm.coerceAtLeast(MinDimensionMm),
        )
    }

    fun toMeters(raw: ArModelDimensionsMm): ArModelDimensionsMeters {
        val safe = safeDimensionsMm(raw)
        return ArModelDimensionsMeters(
            width = safe.widthMm / MillimetersInMeter,
            height = safe.heightMm / MillimetersInMeter,
            depth = safe.depthMm / MillimetersInMeter,
        )
    }

    fun computeUniformScale(
        source: ArModelDimensionsMeters,
        target: ArModelDimensionsMeters,
        policy: ArScalePolicy,
    ): Float {
        val sx = target.width / source.width.coerceAtLeast(MinSourceDimensionMeters)
        val sy = target.height / source.height.coerceAtLeast(MinSourceDimensionMeters)
        val sz = target.depth / source.depth.coerceAtLeast(MinSourceDimensionMeters)
        val candidate = (sx + sy + sz) / 3f
        return policy.clamp(candidate)
    }
}
