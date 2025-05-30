enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://central.sonatype.com/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Butler"

include(":composeApp")
include(":server")
include(":shared:llm")
include(":shared:model")

include(":composeApp:config")

include(":composeApp:core:network")
include(":composeApp:core:network:ktor")
include(":composeApp:core:local")
include(":composeApp:core:local:room")
include(":composeApp:core:local:datastore")
include(":composeApp:core:utils")
include(":composeApp:core:ui:components")
include(":composeApp:core:ui:resources")
include(":composeApp:core:ui:theme")
include(":composeApp:core:ui:utils")
include(":composeApp:core:sync")

include(":composeApp:data:chat")
include(":composeApp:data:credential")
include(":composeApp:data:error")
include(":composeApp:data:host")
include(":composeApp:data:message")
include(":composeApp:data:model")
include(":composeApp:data:resource")
include(":composeApp:data:settings")
include(":composeApp:data:user")

include(":composeApp:domain")
include(":composeApp:domain:chat")
include(":composeApp:domain:audio")
include(":composeApp:domain:auth")
include(":composeApp:domain:host")
include(":composeApp:domain:model")
include(":composeApp:domain:settings")

include(":composeApp:di")
include(":composeApp:di:coroutines")
include(":composeApp:di:datasource")
include(":composeApp:di:repository")

include(":composeApp:feature:auth")
include(":composeApp:feature:chat")
include(":composeApp:feature:error")
include(":composeApp:feature:home")
include(":composeApp:feature:onboarding")
include(":composeApp:feature:permission")
include(":composeApp:feature:profile")
include(":composeApp:feature:theme")
include(":composeApp:feature:preview")
