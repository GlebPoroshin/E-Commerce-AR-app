import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
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
            implementation(projects.common.plp.data)
            implementation(projects.common.plp.domain)
            implementation(projects.common.plp.presentation)
            implementation(projects.common.pdp.data)
            implementation(projects.common.pdp.domain)
            implementation(projects.common.pdp.presentation)
            implementation(projects.common.ar.data)
            implementation(projects.common.ar.domain)
            implementation(projects.common.ar.presentation)
        }
    }
}

android {
    namespace = "com.poroshin.rut.ar.common.umbrella"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
