package com.poroshin.rut.ar.common.ar.presentation

import android.os.Bundle
import com.poroshin.rut.ar.common.ar.domain.ArObjectParams
import com.poroshin.rut.ar.common.ar.domain.ArPlacement

private const val KEY_FILE_PATH = "ar.file_path"
private const val KEY_WIDTH = "ar.width"
private const val KEY_HEIGHT = "ar.height"
private const val KEY_DEPTH = "ar.depth"
private const val KEY_PLACEMENT = "ar.placement"

fun ArObjectParams.toBundle(): Bundle = Bundle().apply {
    putString(KEY_FILE_PATH, filePath)
    putFloat(KEY_WIDTH, widthMm)
    putFloat(KEY_HEIGHT, heightMm)
    putFloat(KEY_DEPTH, depthMm)
    putString(KEY_PLACEMENT, placement.name)
}

fun Bundle.toArObjectParams(): ArObjectParams? {
    val filePath = getString(KEY_FILE_PATH) ?: return null
    val placementName = getString(KEY_PLACEMENT) ?: return null
    if (!containsKey(KEY_WIDTH) || !containsKey(KEY_HEIGHT) || !containsKey(KEY_DEPTH)) {
        return null
    }
    val placement = try {
        ArPlacement.valueOf(placementName)
    } catch (e: IllegalArgumentException) {
        return null
    }
    return ArObjectParams(
        filePath = filePath,
        widthMm = getFloat(KEY_WIDTH),
        heightMm = getFloat(KEY_HEIGHT),
        depthMm = getFloat(KEY_DEPTH),
        placement = placement,
    )
}
