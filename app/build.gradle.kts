plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Signierschlüssel für den Play Store kommt aus Umgebungsvariablen
// (GitHub Actions: Secrets). Ohne sie entsteht nur die Test-Version (debug).
val keystoreFile = System.getenv("BB_KEYSTORE_FILE")

android {
    namespace = "io.github.archonw.bootsbuchung"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.archonw.bootsbuchung"
        minSdk = 24
        targetSdk = 36
        // Bei jedem Store-Upload muss versionCode steigen. In GitHub Actions
        // wird die Nummer des Build-Laufs verwendet.
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "1.0.${System.getenv("GITHUB_RUN_NUMBER") ?: "0"}"
    }

    signingConfigs {
        // Fester Test-Schlüssel (kein Geheimnis): Test-Versionen lassen sich
        // so ohne Deinstallieren aktualisieren und haben einen bekannten
        // Fingerabdruck für die assetlinks.json der Testserver.
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        if (keystoreFile != null) {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("BB_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("BB_KEY_ALIAS")
                keyPassword = System.getenv("BB_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            if (keystoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.google.androidbrowserhelper:androidbrowserhelper:2.6.2")
    implementation("androidx.browser:browser:1.8.0")
}
