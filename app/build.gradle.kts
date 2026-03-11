plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.enigma.fluffyinc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.enigma.fluffyinc"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Biometric dependency
    implementation(libs.androidx.biometric)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")

    // Media3 ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-common:1.7.1")
    implementation("androidx.media3:media3-session:1.7.1")

    // Palette for color extraction
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation ("com.google.code.gson:gson:2.11.0")
    testImplementation(kotlin("test"))

    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // --- PDF & CSV EXPORT ---
    // For PDF Generation (iText7)
    implementation("com.itextpdf:itext7-core:7.2.5")
    // For CSV Generation (OpenCSV)
    implementation("com.opencsv:opencsv:5.7.1")

    // --- COROUTINES ---
    // For background tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- CHARTS ---
    implementation ("ma.hu:compose-charts:0.3.5")

    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")


    // For WebView
    implementation("androidx.webkit:webkit:1.9.0")

    // For document file handling
    implementation("androidx.documentfile:documentfile:1.0.1")


    implementation ("org.slf4j:slf4j-android:1.7.32")

    // JSoup
    implementation("org.jsoup:jsoup:1.17.2")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.core:core-splashscreen:1.0.1")


    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")
    ksp("com.github.bumptech.glide:ksp:4.16.0") // KSP instead of kapt

    // Jackson XML parsing - Best for EPUB structure
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
// Jsoup - Excellent for HTML chapter parsing
    implementation("org.jsoup:jsoup:1.16.1")
// Coroutines for async parsing
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.material:material-icons-core:1.7.8")

    implementation("com.google.accompanist:accompanist-permissions:0.32.0")


    // Charts - using Vico
    implementation("com.patrykandpatrick.vico:compose:2.1.0")
    implementation("com.patrykandpatrick.vico:compose-m3:2.1.0")
    implementation("com.patrykandpatrick.vico:core:2.1.0")
    implementation("co.yml:ycharts:2.1.0")


    // Add to build.gradle
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

}
