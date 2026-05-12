package com.poroshin.rut.ar.common.ar.domain.usecase

import com.poroshin.rut.ar.common.ar.domain.result.RotationResult
import kotlin.math.PI

/**
 * Applies a yaw delta (degrees) to the current Y-axis rotation and returns the normalised result.
 * currentRotation is a FloatArray of Euler angles [pitchDeg, yawDeg, rollDeg].
 */
class RotateModelUseCase {

    operator fun invoke(
        currentRotation: FloatArray,
        deltaDegrees: Float,
    ): RotationResult {
        if (!deltaDegrees.isFinite()) {
            return RotationResult.Rejected("deltaDegrees must be finite, got $deltaDegrees")
        }
        if (currentRotation.size < 3) {
            return RotationResult.Rejected(
                "currentRotation must have at least 3 elements, got ${currentRotation.size}"
            )
        }

        val newYaw = normalizeAngle(currentRotation[1] + deltaDegrees)
        val newRotation = currentRotation.copyOf().also { it[1] = newYaw }
        return RotationResult.Updated(newRotation)
    }

    private fun normalizeAngle(degrees: Float): Float {
        var result = degrees % 360f
        if (result < 0f) result += 360f
        return result
    }
}
