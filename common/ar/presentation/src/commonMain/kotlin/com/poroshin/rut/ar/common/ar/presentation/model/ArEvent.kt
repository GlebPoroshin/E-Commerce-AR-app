package com.poroshin.rut.ar.common.ar.presentation.model

import com.poroshin.rut.ar.common.ar.domain.model.ArPose
import com.poroshin.rut.ar.common.mvi.UiEvent

sealed class ArEvent : UiEvent {
    data object OnCreate : ArEvent()
    data class RotateObject(val deltaDegrees: Float) : ArEvent()
    data class ScaleObject(val factor: Float) : ArEvent()
    data class PlaceObject(val pose: ArPose) : ArEvent()
    data object ClearAll : ArEvent()
    data object RetryModelLoad : ArEvent()
    data class SetSingleMode(val enabled: Boolean) : ArEvent()
    /** Platform-side error string forwarded from the AR session into the MVI stream. */
    data class ShowSceneError(val message: String) : ArEvent()
}
