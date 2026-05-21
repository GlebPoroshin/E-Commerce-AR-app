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
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(projects.common.ar.domain)
            implementation(projects.common.pdp.domain)
            implementation(projects.common.mvi)
        }
        androidMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
            implementation(libs.androidx.fragment)
            implementation(libs.google.arcore)
            implementation(libs.sceneform)
            implementation(libs.koin.android)
            implementation(projects.common.cart.domain)
            implementation(projects.common.cart.presentation)
            implementation(projects.common.core)
        }
    }
}

android {
    namespace = "com.poroshin.rut.ar.common.ar.presentation"
    compileSdk = 35
    defaultConfig {
        minSdk = 28
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
