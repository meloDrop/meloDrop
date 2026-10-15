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
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // Temp URL
            buildConfigField("String", "BASE_URL", "\"https://api.melodrop.example/\"")
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
val adbReverse by tasks.registering(Exec::class) {
    val adb = androidComponents.sdkComponents.adb
    commandLine(adb.get().asFile.absolutePath, "reverse", "tcp:8000", "tcp:8000")
    isIgnoreExitValue = true
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
}