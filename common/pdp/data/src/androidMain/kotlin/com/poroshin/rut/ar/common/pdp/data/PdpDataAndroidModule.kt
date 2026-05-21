package com.poroshin.rut.ar.common.pdp.data

import com.poroshin.rut.ar.common.pdp.data.network.installSharedClient
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

val pdpDataAndroidModule: Module = module {
    single {
        HttpClient(io.ktor.client.engine.okhttp.OkHttp) {
            installSharedClient(get())
        }
    }
}
