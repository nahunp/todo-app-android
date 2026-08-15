plugins {
    // No org.jetbrains.kotlin.android — AGP 9.x's built-in Kotlin support
    // replaces it (see the root build.gradle.kts comment).
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    // Both of these need app/google-services.json to exist — the build will
    // fail with a clear "File google-services.json is missing" error until
    // you add your own (Firebase console -> Project settings -> your app ->
    // download google-services.json). See CLAUDE.md's Firebase section.
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.nahunp.todoapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nahunp.todoapp"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Same runtime-config idea as the web frontend's window.__appConfig /
        // public/config.js (see the web repo's runtime-config.ts doc
        // comment) — but for a native app, build-time BuildConfig fields are
        // the right tool, not a runtime-fetched file: there's no equivalent
        // of "redeploy without rebuilding" for an installed APK the way
        // there is for a static web bundle. Override per build type below,
        // not hardcoded once here.
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5080\"")
    }

    buildTypes {
        debug {
            // 10.0.2.2 is the Android emulator's alias for the host
            // machine's localhost — for a physical device on the same
            // network as the local backend, override this per-run with
            // -PapiBaseUrl=http://<host-lan-ip>:5080 instead of editing
            // this file.
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5080\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Same production API origin the web frontend's config.js points
            // at — see the web repo's README.md Deployment section.
            buildConfigField("String", "API_BASE_URL", "\"https://todoapp-api-us3zbx.azurewebsites.net\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
