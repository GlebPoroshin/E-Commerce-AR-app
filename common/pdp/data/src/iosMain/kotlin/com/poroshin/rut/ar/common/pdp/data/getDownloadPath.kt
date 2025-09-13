package com.poroshin.rut.ar.common.pdp.data

import kotlinx.io.files.Path
import platform.Foundation.*

actual fun getDownloadPath(fileName: String): Path {
    val dirs = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory, NSUserDomainMask, true
    )
    val cacheDir = dirs.first() as String
    return Path("$cacheDir/$fileName")
}
