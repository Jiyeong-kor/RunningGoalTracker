import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.com.google.gms.google.services)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jeong.runninggoaltracker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.jeong.runninggoaltracker"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":shared:designsystem"))
    implementation(project(":shared:navigation"))
    implementation(project(":feature:home"))
    implementation(project(":feature:goal"))
    implementation(project(":feature:record"))
    implementation(project(":feature:reminder"))
    implementation(project(":feature:mypage"))

    // core / lifecycle / activity / viewmodel-compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Compose UI
    implementation(libs.bundles.androidx.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Firebase
    implementation(libs.firebase.analytics)
    implementation(platform(libs.firebase.bom))

    // unit test
    testImplementation(libs.junit)

    // androidTest
    androidTestImplementation(libs.bundles.androidx.compose.test)
    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    kspAndroidTest(libs.dagger.hilt.compiler)

    // debug
    debugImplementation(libs.bundles.androidx.compose.debug)
}
