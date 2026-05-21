package com.poroshin.rut.ar.common.pdp.data

import com.poroshin.rut.ar.common.pdp.data.network.installSharedClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

val pdpDataIOSModule: Module = module {
    single {
        HttpClient(Darwin) {
            installSharedClient(get())
        }
    }
}
