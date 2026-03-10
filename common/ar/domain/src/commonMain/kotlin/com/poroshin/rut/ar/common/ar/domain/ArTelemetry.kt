package com.poroshin.rut.ar.common.ar.domain

enum class ArTelemetryEvent {
    PlacementSuccess,
    PlacementRejectedSurface,
    PlacementRejectedCollision,
    TrackingLost,
    RelocalizationSuccess,
}

data class ArPerformanceSnapshot(
    val timeToFirstPlacementMs: Long?,
    val placementRetries: Int,
    val activeEntities: Int,
)
