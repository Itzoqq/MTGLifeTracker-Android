plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mtglifetracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mtglifetracker"
        minSdk = 24
        targetSdk = 35
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
    // ADD THIS BLOCK TO ENABLE VIEW BINDING
    buildFeatures {
        viewBinding = true
    }
}

// THIS IS THE BLOCK WHERE YOU ADD THE NEW DEPENDENCIES
dependencies {
    // AndroidX & Material Components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // ViewModel and Lifecycle Dependencies (NEW)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.fragment:fragment-ktx:1.8.0") // Provides the "by viewModels()" delegate

    // NEW: Add the Gson library for serializing data
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}