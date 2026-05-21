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
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lint {
        disable += "NullSafeMutableLiveData"
    }
}

dependencies {
    implementation(projects.common.umbrella)

    implementation("io.coil-kt.coil3:coil:3.2.0")
    implementation("io.coil-kt.coil3:coil-svg:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")

    implementation(libs.compose.ui)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.cicerone)

    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.fragment)

    debugImplementation(libs.compose.ui.tooling)

    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation(libs.compose.ui.test.manifest)
}
