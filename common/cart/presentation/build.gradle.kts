import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    if (System.getenv("XCODE_APP_SUPPORT_DIR") == null) {
        alias(libs.plugins.compose.compiler)
        id("org.jetbrains.compose") version "1.10.0-alpha01"
    }
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.common.cart.domain)
            api(projects.common.mvi)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.androidx.lifecycle.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.cicerone)
            implementation(projects.common.core)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.coil.compose)
        }
    }
}

android {
    namespace = "com.poroshin.rut.ar.common.cart.presentation"
    compileSdk = 35
    defaultConfig {
        minSdk = 28
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
