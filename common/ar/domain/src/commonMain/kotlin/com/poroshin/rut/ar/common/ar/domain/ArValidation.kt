package com.poroshin.rut.ar.common.ar.domain

enum class ArPlacementErrorCode {
    InvalidDimensions,
    PlaneNotAllowed,
    CollisionDetected,
    ModelLoadingFailed,
    TrackingLost,
    ArUnavailable,
    PlacementLimitReached,
}

enum class ArTrackingStatus {
    SearchingSurface,
    Tracking,
    Lost,
}

sealed class ArValidationResult {
    data object Valid : ArValidationResult()

    data class Invalid(
        val code: ArPlacementErrorCode,
    ) : ArValidationResult()
}

object ArDimensionValidator {
    private const val MinValidDimensionMm = 1f

    fun validate(raw: ArModelDimensionsMm): ArValidationResult {
        if (!raw.widthMm.isFinite() || !raw.heightMm.isFinite() || !raw.depthMm.isFinite()) {
            return ArValidationResult.Invalid(ArPlacementErrorCode.InvalidDimensions)
        }
        if (raw.widthMm < MinValidDimensionMm ||
            raw.heightMm < MinValidDimensionMm ||
            raw.depthMm < MinValidDimensionMm
        ) {
            return ArValidationResult.Invalid(ArPlacementErrorCode.InvalidDimensions)
        }
        return ArValidationResult.Valid
    }
}
