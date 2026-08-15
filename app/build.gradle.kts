import java.util.Properties

plugins {
    // No org.jetbrains.kotlin.android — AGP 9.x's built-in Kotlin support
    // replaces it (see the root build.gradle.kts comment).
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    // apply false here, applied conditionally below — both plugins fail the
    // ENTIRE build hard (not just Firebase features) if app/
    // google-services.json doesn't exist, which made this project
    // unbuildable out of the box before a real Firebase project was set
    // up. Confirmed live: without this, `./gradlew :app:assembleDebug`
    // fails at :app:processDebugGoogleServices before compiling a single
    // Kotlin file, which is exactly the error Diego hit trying to run this
    // in Android Studio.
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

// See the comment above — only wire up Firebase once there's a real
// project config to wire up. Firebase's SDKs (dependencies below) are
// still on the classpath either way, so code referencing them still
// compiles; they just won't initialize without this. Get your own
// google-services.json from the Firebase console and drop it in app/ —
// see CLAUDE.md's Firebase section — and this flips on automatically,
// no other change needed.
val hasFirebaseConfig = file("google-services.json").exists()
if (hasFirebaseConfig) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
} else {
    logger.warn(
        "app/google-services.json not found — building without Firebase " +
            "(Crashlytics/Analytics/Messaging won't initialize). See " +
            "CLAUDE.md's Firebase section to set up a real project."
    )
}

// Release signing — keystore.properties (project root, gitignored, never
// committed — see CLAUDE.md's "Release signing" section) holds the actual
// keystore path/passwords. Loaded conditionally, same reasoning as the
// Firebase config above: CI has no keystore and shouldn't need one — a
// release build there is for catching R8/shrinking regressions early
// (isMinifyEnabled below), not for producing something to actually
// upload, so it's allowed to come out unsigned rather than failing the
// whole build the way the Firebase plugins used to.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = if (keystorePropertiesFile.exists()) {
    Properties().apply { load(keystorePropertiesFile.inputStream()) }
} else {
    null
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

        // The frontend's static site (not the API — a different Azure
        // resource entirely), specifically frontend/public/
        // mobile-captcha.html in the web repo. Same for every build type,
        // unlike API_BASE_URL below — there's no "local" version of this
        // worth standing up; the deployed page works fine for local dev
        // too and there's nothing to run locally to replace it. See
        // TurnstileCaptchaView.kt for how this gets used.
        buildConfigField(
            "String",
            "CAPTCHA_PAGE_URL",
            "\"https://zealous-meadow-0c73a9610.7.azurestaticapps.net/mobile-captcha.html\"",
        )
    }

    // Same runtime-config idea as the web frontend's window.__appConfig /
    // public/config.js (see the web repo's runtime-config.ts doc comment)
    // — but for a native app, build-time BuildConfig fields are the right
    // tool, not a runtime-fetched file: there's no equivalent of "redeploy
    // without rebuilding" for an installed APK the way there is for a
    // static web bundle.
    //
    // Debug default is the REAL production backend, not localhost — a
    // real phone (physical device, not the emulator) can't reach
    // 10.0.2.2 at all (that address only means anything to the emulator),
    // and testing against production is what actually happened first
    // (confirmed live: "server not reachable" on a real device against
    // the emulator-only address). For local-backend testing instead —
    // emulator, or a real device on the same Wi-Fi as the dev machine —
    // override per-run rather than editing this file:
    //   ./gradlew :app:installDebug -PapiBaseUrl=http://10.0.2.2:5080
    //   ./gradlew :app:installDebug -PapiBaseUrl=http://<host-LAN-ip>:5080
    val apiBaseUrl = (project.findProperty("apiBaseUrl") as String?)
        ?: "https://todoapp-api-us3zbx.azurewebsites.net"

    signingConfigs {
        if (keystoreProperties != null) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Same production API origin the web frontend's config.js points
            // at — see the web repo's README.md Deployment section.
            buildConfigField("String", "API_BASE_URL", "\"https://todoapp-api-us3zbx.azurewebsites.net\"")
            if (keystoreProperties != null) {
                signingConfig = signingConfigs.getByName("release")
            }
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
