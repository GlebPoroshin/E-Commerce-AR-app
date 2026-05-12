package com.poroshin.rut.ar.common.ar.domain.usecase

import com.poroshin.rut.ar.common.ar.domain.ArAabb
import com.poroshin.rut.ar.common.ar.domain.ArCollisionEvaluator
import com.poroshin.rut.ar.common.ar.domain.ArPlacementPolicy
import com.poroshin.rut.ar.common.ar.domain.ArPlaneType
import com.poroshin.rut.ar.common.ar.domain.ArVector3
import com.poroshin.rut.ar.common.ar.domain.model.ArPose
import com.poroshin.rut.ar.common.ar.domain.result.PlacementResult
import com.poroshin.rut.ar.common.pdp.domain.ArPlacement

/**
 * Validates and commits an object placement using [ArCollisionEvaluator] and [ArPlacementPolicy].
 *
 * Parameters:
 * [pose]            — desired pose for the new object.
 * [halfExtents]     — half-extents (in metres) of the new object's AABB; derived from geometry.
 * [alreadyPlaced]   — list of (pose, halfExtents) pairs for objects already on the scene.
 * [placement]       — placement type from product AR info (FLOOR, CEILING, etc.).
 * [planeType]       — the AR plane type that the raycast hit.
 * [isSingleMode]    — when true, any already-placed objects count as a limit violation.
 *
 * TODO(Phase 3B): [halfExtents] and [alreadyPlaced] items' half-extents should come from the
 * platform AR session (Sceneform / RealityKit node bounds). Wire these in ArSceneController
 * during Phase 3B once the native node geometry is available.
 */
class PlaceModelUseCase {

    operator fun invoke(
        pose: ArPose,
        halfExtents: ArVector3,
        alreadyPlaced: List<Pair<ArPose, ArVector3>>,
        placement: ArPlacement,
        planeType: ArPlaneType,
        isSingleMode: Boolean = false,
    ): PlacementResult {
        if (isSingleMode && alreadyPlaced.isNotEmpty()) {
            return PlacementResult.Rejected("Single-object mode: scene already has a placed object.")
        }

        if (!ArPlacementPolicy.supportsPlane(placement, planeType)) {
            return PlacementResult.Rejected(
                "Plane type $planeType is not allowed for placement policy $placement."
            )
        }

        val newAabb = pose.toAabb(halfExtents)
        for ((existingPose, existingHalfExtents) in alreadyPlaced) {
            val existingAabb = existingPose.toAabb(existingHalfExtents)
            if (ArCollisionEvaluator.intersects(newAabb, existingAabb)) {
                return PlacementResult.Rejected("Placement rejected: collision with an existing object.")
            }
        }

        return PlacementResult.Updated(pose)
    }

    private fun ArPose.toAabb(halfExtents: ArVector3): ArAabb {
        val t = translation
        val cx = if (t.size > 0) t[0] else 0f
        val cy = if (t.size > 1) t[1] else 0f
        val cz = if (t.size > 2) t[2] else 0f
        return ArAabb(
            center = ArVector3(cx, cy, cz),
            halfExtents = halfExtents,
        )
    }
}
