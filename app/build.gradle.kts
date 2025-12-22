plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
}

android {
    namespace = "com.dinzio.bookingapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.dinzio.bookingapp"
        minSdk = 24
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
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    /// CORE ANDROID & COMPOSE
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    /// DEPENDENCY INJECTION (Hilt)
//    implementation(libs.hilt.android)
//    kapt(libs.hilt.android.compiler)
//    implementation(libs.androidx.hilt.navigation.compose)

    /// NETWORKING (Retrofit & OkHttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

//    /// LOCAL DATABASE (Room & DataStore)
//    val roomVersion = "2.6.1"
//    implementation("androidx.room:room-runtime:$roomVersion")
//    implementation("androidx.room:room-ktx:$roomVersion")
//    kapt("androidx.room:room-compiler:$roomVersion")
//    implementation("androidx.datastore:datastore-preferences:1.0.0")
//
//    /// WORK MANAGER
//    implementation("androidx.work:work-runtime-ktx:2.9.0")
//    implementation("androidx.hilt:hilt-work:1.1.0")
//    kapt("androidx.hilt:hilt-compiler:1.1.0")
//
    /// FIREBASE
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.crashlytics.ktx)
    implementation(libs.google.firebase.analytics.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}