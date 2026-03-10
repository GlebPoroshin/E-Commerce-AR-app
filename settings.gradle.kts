enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "E-Commerce_AR_app"
include(":androidApp")

include(":common")

include(":common:core")

include(":common:mvi")
include(":common:umbrella")

include(":common:cart:domain")
include(":common:cart:data")
include(":common:cart:presentation")

include(":common:plp:domain")
include(":common:plp:data")
include(":common:plp:presentation")

include(":common:pdp:domain")
include(":common:pdp:data")
include(":common:pdp:presentation")

include(":common:ar:domain")
include(":common:ar:data")
include(":common:ar:presentation")
