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
    }
}

rootProject.name = "the-croncher"

// :core is pure Kotlin/JVM and cannot see the Android SDK — that boundary is
// what keeps the game simulation unit-testable without a device.
include(":core")
include(":app")
