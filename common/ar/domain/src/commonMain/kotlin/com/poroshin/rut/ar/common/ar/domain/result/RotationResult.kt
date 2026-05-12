package com.poroshin.rut.ar.common.ar.domain.result

sealed class RotationResult {
    data class Updated(val newRotation: FloatArray) : RotationResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Updated) return false
            return newRotation.contentEquals(other.newRotation)
        }

        override fun hashCode(): Int = newRotation.contentHashCode()
    }

    data class Rejected(val reason: String) : RotationResult()
}
