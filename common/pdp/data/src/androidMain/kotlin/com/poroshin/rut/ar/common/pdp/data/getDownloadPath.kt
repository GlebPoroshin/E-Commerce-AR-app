package com.poroshin.rut.ar.common.pdp.data

import android.content.Context
import kotlinx.io.files.Path
import org.koin.core.context.GlobalContext

actual fun getDownloadPath(fileName: String): Path {
    val context: Context = GlobalContext.get().get<Context>()
    val file = context.cacheDir.resolve(fileName)

    return Path(file.absolutePath)
}
