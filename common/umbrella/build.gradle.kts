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
            implementation(projects.common.ar.data)
            implementation(projects.common.ar.domain)
            api(projects.common.ar.presentation)

            implementation(projects.common.plp.data)
            implementation(projects.common.plp.domain)
            api(projects.common.plp.presentation)

            implementation(projects.common.pdp.data)
            implementation(projects.common.pdp.domain)
            api(projects.common.pdp.presentation)

            api(projects.common.mvi)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core)
        }

        iosMain.dependencies {
            api(projects.common.ar.data)
            api(projects.common.ar.domain)

            api(projects.common.plp.data)
            api(projects.common.plp.domain)

            api(projects.common.pdp.data)
            api(projects.common.pdp.domain)

            api(projects.common.mvi)
        }
    }

    cocoapods {
        summary = "App KMM framework"
        homepage = "https://example"
        version = "1.0.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "ARApp"
            isStatic = true
            export(projects.common.mvi)

            export(projects.common.ar.domain)
            export(projects.common.ar.presentation)

            export(projects.common.plp.domain)
            export(projects.common.plp.presentation)

            export(projects.common.pdp.domain)
            export(projects.common.pdp.presentation)
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
