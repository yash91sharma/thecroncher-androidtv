import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// The Play upload key. It lives inside the sealed toolchain, never in the repo:
// generate it with `keytool` into .toolchain/keys/ and describe it in
// keystore.properties (see README, "Release build"). When the file is absent —
// a fresh checkout, CI — release builds fall back to the debug key so they still
// build and can be sideloaded for testing, but Play would reject them.
val keystoreProperties = rootProject.file(".toolchain/keys/keystore.properties")
val hasUploadKey = keystoreProperties.exists()

android {
    namespace = "com.yash.thecroncher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yash.thecroncher"
        // 26 is where SurfaceHolder.lockHardwareCanvas arrived; Android TV boxes
        // older than 8.0 are not a market worth a runtime version branch.
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (hasUploadKey) {
            create("release") {
                val props = Properties().apply {
                    keystoreProperties.inputStream().use { load(it) }
                }
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        // Debug-signed, not unsigned: Android's package manager rejects genuinely
        // unsigned APKs outright. The debug keystore is generated under
        // ANDROID_USER_HOME, which env.sh pins inside .toolchain/.
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
        }

        // What goes to Play: shrunk and obfuscated by R8, signed with the upload key.
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (hasUploadKey) {
                signingConfigs.getByName("release")
            } else {
                logger.warn(
                    "No upload key at ${keystoreProperties.path}; " +
                        "release builds will be signed with the debug key.",
                )
                signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures {
        // BuildConfig.DEBUG gates the logging; AGP 8 no longer generates it by default.
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    implementation(project(":core"))
    // 1.13.1 is the newest androidx.core that builds against compileSdk 34.
    implementation("androidx.core:core-ktx:1.13.1")
}
