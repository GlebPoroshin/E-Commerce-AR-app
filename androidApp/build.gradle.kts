plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.poroshin.rut.ar.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.poroshin.rut.ar.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.common.umbrella)
    implementation(projects.common.pdp.data)

    implementation(projects.common.pdp.presentation)
    implementation(projects.common.plp.presentation)
    implementation(projects.common.ar.presentation)

    implementation(projects.common.pdp.domain)
    implementation(projects.common.plp.domain)
    implementation(projects.common.ar.domain)

    implementation(libs.compose.ui)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.cicerone)

    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.fragment)
    implementation(libs.cicerone)

    debugImplementation(libs.compose.ui.tooling)
}