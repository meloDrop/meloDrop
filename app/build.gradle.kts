plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.app.melodrop"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.app.melodrop"
        minSdk = 26
        targetSdk = 37
        versionCode = 3
        versionName = "v.0.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // Production backend, hosted on Scaleway
            buildConfigField("String", "BASE_URL", "\"https://api.melodrop-app.com/\"")
        }
        debug {
            buildConfigField("String", "BASE_URL", "\"http://localhost:8000/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Redirige le port 8000 du téléphone vers celui du PC avant chaque build debug
val adbReverse by tasks.registering {
    val adb = androidComponents.sdkComponents.adb
    doLast {
        val adbPath = adb.get().asFile.absolutePath
        val serials = ProcessBuilder(adbPath, "devices")
            .start()
            .inputStream.bufferedReader().readLines()
            .filter { it.endsWith("\tdevice") }
            .map { it.substringBefore("\t") }
        serials.forEach { serial ->
            ProcessBuilder(adbPath, "-s", serial, "reverse", "tcp:8000", "tcp:8000")
                .inheritIO()
                .start()
                .waitFor()
        }
    }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    dependsOn(adbReverse)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)


    // Network : Retrofit + OkHttp

    // Retrofit: turns a Kotlin interface into HTTP calls to the FastAPI backend
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // Gson converter: converts JSON responses into Kotlin objects (and objects into JSON for requests)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp: HTTP client used by Retrofit to send requests (connections, timeouts, interceptors)
    // Declared explicitly so its version matches logging-interceptor
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Logging interceptor: prints requests and responses in Logcat (debug only)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Token stock
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // ViewModel integration for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    // Navigation Bar
    implementation("androidx.navigation:navigation-compose:2.10.2")

    // Images: loads the covers from their URL
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")

    // Lets Coil download the images with OkHttp
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    // Audio: plays the 30-second previews
    implementation("androidx.media3:media3-exoplayer:1.8.0")

// Lifecycle events in Compose: stops the music when the app goes to the background
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")

}