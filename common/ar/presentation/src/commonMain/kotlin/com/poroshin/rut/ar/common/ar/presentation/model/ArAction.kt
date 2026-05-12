package com.poroshin.rut.ar.common.ar.presentation.model

import com.poroshin.rut.ar.common.mvi.UiAction

sealed class ArAction : UiAction {
    data class ShowError(val message: String) : ArAction()
    data object NavigateBack : ArAction()
    data class LogTelemetry(
        val name: String,
        val params: Map<String, String> = emptyMap(),
    ) : ArAction()
    /** Instructs the platform AR scene to remove all placed nodes. */
    data object TriggerClearScene : ArAction()
    /** Instructs the platform AR scene to reload the 3D model. */
    data object TriggerReloadModel : ArAction()
}
