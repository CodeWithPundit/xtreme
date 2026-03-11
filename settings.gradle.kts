pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "XtremeIPTV"

// Core modules
include(":core:common")
include(":core:data")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:preferences")
include(":core:testing")

// Feature modules
include(":feature:auth")
include(":feature:home")
include(":feature:live")
include(":feature:movies")
include(":feature:series")
include(":feature:epg")
include(":feature:player")
include(":feature:download")
include(":feature:recording")
include(":feature:profile")

// Service modules
include(":service:sync")
include(":service:download")
include(":service:recording")

// App module
include(":app")
