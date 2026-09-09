plugins {
    id("org.jetbrains.kotlin.jvm")
}

// Pure Kotlin/JVM. No Android dependency may ever be added here — the whole
// point is that `./gradlew :core:test` runs the entire game simulation on the
// JVM in seconds, with no emulator and no device.
kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
