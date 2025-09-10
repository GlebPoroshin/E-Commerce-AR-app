import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
     alias(libs.plugins.kotlinCocoapods)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.common.ar.data)
            api(projects.common.ar.domain)
            api(projects.common.ar.presentation)
            api(projects.common.plp.data)
            api(projects.common.plp.domain)
            api(projects.common.plp.presentation)
            api(projects.common.pdp.data)
            api(projects.common.pdp.domain)
            api(projects.common.pdp.presentation)
            api(projects.common.mvi)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.runtime)
        }
        iosMain.dependencies {}
    }

    cocoapods {
        summary = "App KMM framework"
        homepage = "https://example"
        version = "1.0.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "ARApp"
            isStatic = true
            export(project(":common:mvi"))
            export(project(":common:plp"))
            export(project(":common:pdp"))
            export(project(":common:ar"))
        }
    }
}

android {
    namespace = "com.poroshin.rut.common.umbrella"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
