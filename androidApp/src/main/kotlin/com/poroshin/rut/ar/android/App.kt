package com.poroshin.rut.ar.android

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.poroshin.rut.ar.common.cart.domain.usecase.RunLegacyModelVersionsMigrationUseCase
import com.poroshin.rut.ar.common.core.BackendConfig
import com.poroshin.rut.ar.common.umbrella.di.androidPlatformModule
import com.poroshin.rut.ar.common.umbrella.di.umbrellaCommonModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        BackendConfig.setUseMockFallback(isDebuggable)

        startKoin {
            modules(
                umbrellaCommonModules()
                    + androidPlatformModule
                    + module { single<Context> { this@App } }
            )
        }

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            GlobalContext.get().get<RunLegacyModelVersionsMigrationUseCase>().invoke()
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
