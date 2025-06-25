
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nocturna.votechain"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nocturna.votechain"
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.espresso.core)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.androidx.navigation.compose)

    // Retrofit and network dependencies
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    // Gson for JSON parsing
    implementation (libs.gson)

    // Coroutines for asynchronous programming
    implementation (libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // ViewModel and LiveData
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Coil for image loading
    implementation (libs.coil.compose)
    implementation (libs.coil.gif)

    // Web3j dependencies for Ethereum blockchain interaction
    implementation (libs.core)
    implementation (libs.crypto)
    implementation (libs.utils)

    // Testing
    androidTestImplementation (libs.androidx.junit.v115)
    androidTestImplementation (libs.ui.test.junit4)
    debugImplementation (libs.ui.tooling)
    debugImplementation (libs.ui.test.manifest)

    // Security library
    implementation (libs.androidx.security.crypto)

    //Email functionality
    implementation(libs.javax.mail)

    // BouncyCastle dependencies
    implementation (libs.bcprov.jdk15on)
    implementation (libs.bcpkix.jdk15on)

    // JUnit 5 Testing
    testImplementation (libs.junit.jupiter.api)
    testImplementation (libs.junit.jupiter.engine)
    testImplementation (libs.junit.jupiter.params)

    // Android Architecture Components Testing
    testImplementation (libs.androidx.core.testing)

    // Coroutines Testing
    testImplementation (libs.kotlinx.coroutines.test)

    // Mockito untuk mocking
    testImplementation (libs.mockito.core)
    testImplementation (libs.mockito.inline)
    testImplementation (libs.mockito.kotlin)

    // MockK untuk Kotlin mocking (alternative yang lebih baik untuk Kotlin)
    testImplementation (libs.mockk)
    testImplementation (libs.mockk.android)

    // Robolectric untuk Android unit testing
    testImplementation (libs.robolectric)

    // Truth assertion library (Google)
    testImplementation (libs.truth)

    // Turbine untuk Flow testing
    testImplementation (libs.turbine)

    // Android Test (untuk instrumentation tests jika diperlukan)
    androidTestImplementation (libs.junit.v115)
    androidTestImplementation (libs.androidx.espresso.core.v351)
    androidTestImplementation (libs.androidx.compose.ui.ui.test.junit4)

    // ViewModel testing
    testImplementation (libs.androidx.lifecycle.viewmodel.ktx)

    // LiveData testing
    testImplementation (libs.androidx.lifecycle.livedata.ktx.v270)
}