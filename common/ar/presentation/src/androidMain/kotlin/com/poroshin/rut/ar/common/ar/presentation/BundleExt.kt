package com.poroshin.rut.ar.common.ar.presentation

import android.os.Bundle
import com.poroshin.rut.ar.common.ar.domain.ArObjectParams

private const val KEY_FILE_PATH = "ar.file_path"
private const val KEY_WIDTH = "ar.width"
private const val KEY_HEIGHT = "ar.height"
private const val KEY_DEPTH = "ar.depth"

fun ArObjectParams.toBundle(): Bundle = Bundle().apply {
    putString(KEY_FILE_PATH, filePath)
    putFloat(KEY_WIDTH, widthMm)
    putFloat(KEY_HEIGHT, heightMm)
    putFloat(KEY_DEPTH, depthMm)
}

fun Bundle.toArObjectParams(): ArObjectParams? {
    val filePath = getString(KEY_FILE_PATH) ?: return null
    if (!containsKey(KEY_WIDTH) || !containsKey(KEY_HEIGHT) || !containsKey(KEY_DEPTH)) {
        return null
    }
    return ArObjectParams(
        filePath = filePath,
        widthMm = getFloat(KEY_WIDTH),
        heightMm = getFloat(KEY_HEIGHT),
        depthMm = getFloat(KEY_DEPTH),
    )
}
