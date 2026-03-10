package com.poroshin.rut.ar.common.ar.domain

import com.poroshin.rut.ar.common.pdp.domain.ArPlacement

enum class ArPlaneType {
    HorizontalUpward,
    HorizontalDownward,
    Vertical,
}

enum class ArRaycastAlignment {
    Horizontal,
    Vertical,
}

data class ArDistancePolicy(
    val minDistanceMeters: Float,
    val maxDistanceMeters: Float,
) {
    init {
        require(minDistanceMeters > 0f) { "minDistanceMeters must be > 0" }
        require(maxDistanceMeters >= minDistanceMeters) {
            "maxDistanceMeters must be >= minDistanceMeters"
        }
    }

    fun clamp(distanceMeters: Float): Float {
        if (!distanceMeters.isFinite()) return minDistanceMeters
        return distanceMeters.coerceIn(minDistanceMeters, maxDistanceMeters)
    }

    companion object {
        val Default = ArDistancePolicy(
            minDistanceMeters = 0.35f,
            maxDistanceMeters = 4.0f,
        )
    }
}

data class ArScalePolicy(
    val minScale: Float,
    val maxScale: Float,
) {
    init {
        require(minScale > 0f) { "minScale must be > 0" }
        require(maxScale >= minScale) { "maxScale must be >= minScale" }
    }

    fun clamp(scale: Float): Float {
        if (!scale.isFinite()) return minScale
        return scale.coerceIn(minScale, maxScale)
    }

    companion object {
        val Default = ArScalePolicy(
            minScale = 0.01f,
            maxScale = 100f,
        )
    }
}

object ArPlacementPolicy {
    fun supportsPlane(placement: ArPlacement, planeType: ArPlaneType): Boolean {
        return when (placement) {
            ArPlacement.ANY_SURFACE -> true
            ArPlacement.ANY_HORIZONTAL -> planeType == ArPlaneType.HorizontalUpward ||
                planeType == ArPlaneType.HorizontalDownward
            ArPlacement.ANY_VERTICAL -> planeType == ArPlaneType.Vertical
            ArPlacement.FLOOR -> planeType == ArPlaneType.HorizontalUpward
            ArPlacement.CEILING -> planeType == ArPlaneType.HorizontalDownward
        }
    }

    fun allowedRaycastAlignments(placement: ArPlacement): Set<ArRaycastAlignment> {
        return when (placement) {
            ArPlacement.ANY_SURFACE -> setOf(ArRaycastAlignment.Horizontal, ArRaycastAlignment.Vertical)
            ArPlacement.ANY_HORIZONTAL,
            ArPlacement.FLOOR,
            ArPlacement.CEILING -> setOf(ArRaycastAlignment.Horizontal)
            ArPlacement.ANY_VERTICAL -> setOf(ArRaycastAlignment.Vertical)
        }
    }
}
