package com.poroshin.rut.ar.common.ar.presentation.model

import com.poroshin.rut.ar.common.ar.domain.model.ArPose
import com.poroshin.rut.ar.common.mvi.UiState

/**
 * Immutable UI state for the AR screen.
 *
 * [modelUrl]       — remote URL of the 3D model being loaded / displayed.
 * [isLoading]      — true while the model download or AR init is in progress.
 * [isModelLoaded]  — true after the model has been successfully loaded into the scene.
 * [error]          — last human-readable error message; null when there is no error.
 * [placedObjects]  — ordered list of poses of all currently placed objects.
 * [isSingleMode]   — when true only one object is allowed on the scene at a time.
 * [currentScale]   — uniform scale currently applied to the active node (1.0 = native size).
 * [currentRotation]— Euler angles [pitchDeg, yawDeg, rollDeg] of the active node.
 */
data class ArState(
    val modelUrl: String = "",
    val isLoading: Boolean = false,
    val isModelLoaded: Boolean = false,
    val error: String? = null,
    val placedObjects: List<ArPose> = emptyList(),
    val isSingleMode: Boolean = true,
    val currentScale: Float = 1.0f,
    val currentRotation: FloatArray = floatArrayOf(0f, 0f, 0f),
) : UiState {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArState) return false
        return modelUrl == other.modelUrl &&
            isLoading == other.isLoading &&
            isModelLoaded == other.isModelLoaded &&
            error == other.error &&
            placedObjects == other.placedObjects &&
            isSingleMode == other.isSingleMode &&
            currentScale == other.currentScale &&
            currentRotation.contentEquals(other.currentRotation)
    }

    override fun hashCode(): Int {
        var result = modelUrl.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isModelLoaded.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + placedObjects.hashCode()
        result = 31 * result + isSingleMode.hashCode()
        result = 31 * result + currentScale.hashCode()
        result = 31 * result + currentRotation.contentHashCode()
        return result
    }
}
