package com.poroshin.rut.ar.common.pdp.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.Module
import org.koin.dsl.module

val pdpDataAndroidModule: Module = module {
    single {
        HttpClient(io.ktor.client.engine.okhttp.OkHttp) {
            install(ContentNegotiation) {
                json()
            }
        }
    }
}
