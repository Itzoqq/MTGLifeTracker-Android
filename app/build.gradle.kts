plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("jacoco")
}

// Configure the JaCoCo version at the top level
jacoco {
    toolVersion = libs.versions.jacoco.get()
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

        testInstrumentationRunner = "com.example.mtglifetracker.HiltTestRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs(
                    "-XX:+EnableDynamicAgentLoading",
                    "-Djdk.instrument.traceUsage"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // Enable coverage for unit and instrumented tests using the new properties
        getByName("debug") {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
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
        viewBinding = true
    }
}

configurations.all {
    resolutionStrategy {
        force(libs.androidx.test.monitor)
    }
}

dependencies {
    // AndroidX & Material Components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // ViewModel and Lifecycle Dependencies
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Room Database Dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    // CHANGED: Switched from kapt to ksp
    ksp(libs.hilt.android.compiler)

    // --- Dependencies for local Unit Tests (for ViewModel, etc.) ---
    testImplementation(libs.junit)
    testImplementation(libs.core.ktx)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.room.testing)

    // --- Dependencies for Android Instrumentation (UI) Tests ---
    androidTestImplementation(libs.core.ktx)
    androidTestImplementation(libs.androidx.test.monitor)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.fragment.testing)
    implementation(libs.androidx.espresso.idling.resource)

    androidTestImplementation(libs.androidx.espresso.contrib)

    kspAndroidTest(libs.hilt.android.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("dagger.fastInit", "enabled")
    arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
}


tasks.register("runAllTests") {
    group = "verification"
    description = "Runs all unit and instrumented tests for the debug build."
    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest", "createDebugCoverageReport")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        // Android
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        // Hilt
        "**/*_HiltModules*.*",
        "**/Dagger*Component.*",
        "**/*Module_*",
        "**/*_Factory.*",
        "**/*_Provide*Factory*.*",
        "**/*_ViewBinding*.*",
        // Others
        "**/*Directions$*",
        "**/*Directions.*",
    )

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(
        fileTree("${layout.buildDirectory.get().asFile}/classes/kotlin/debug") {
            exclude(fileFilter)
        }
    )
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/*/*.ec"
            )
        }
    )
}