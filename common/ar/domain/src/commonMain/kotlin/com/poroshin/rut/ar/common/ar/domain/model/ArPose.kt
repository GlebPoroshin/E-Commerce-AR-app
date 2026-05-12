package com.poroshin.rut.ar.common.ar.domain.model

/**
 * Platform-neutral 3D pose: position (translation) and orientation (rotation quaternion).
 * Both arrays are fixed-size: translation = [x, y, z], rotation = [x, y, z, w].
 */
data class ArPose(
    val translation: FloatArray,
    val rotation: FloatArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArPose) return false
        return translation.contentEquals(other.translation) &&
            rotation.contentEquals(other.rotation)
    }

    override fun hashCode(): Int {
        var result = translation.contentHashCode()
        result = 31 * result + rotation.contentHashCode()
        return result
    }
}
