package com.poroshin.rut.ar.common.pdp.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.Module
import org.koin.dsl.module

val pdpDataIOSdModule: Module = module {
    single {
        HttpClient(Darwin) {
            install(ContentNegotiation) {
                json()
            }
        }
    }
}
