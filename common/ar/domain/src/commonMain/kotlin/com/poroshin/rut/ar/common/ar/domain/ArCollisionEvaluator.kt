package com.poroshin.rut.ar.common.ar.domain

import kotlin.math.abs

data class ArVector3(
    val x: Float,
    val y: Float,
    val z: Float,
)

data class ArAabb(
    val center: ArVector3,
    val halfExtents: ArVector3,
)

object ArCollisionEvaluator {
    fun intersects(lhs: ArAabb, rhs: ArAabb): Boolean {
        val dx = abs(lhs.center.x - rhs.center.x)
        val dy = abs(lhs.center.y - rhs.center.y)
        val dz = abs(lhs.center.z - rhs.center.z)

        return dx <= (lhs.halfExtents.x + rhs.halfExtents.x) &&
            dy <= (lhs.halfExtents.y + rhs.halfExtents.y) &&
            dz <= (lhs.halfExtents.z + rhs.halfExtents.z)
    }
}
