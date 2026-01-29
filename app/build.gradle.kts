import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

val debugStorePassword: String =
    localProperties.getProperty("DEBUG_STORE_PASSWORD")
        ?: error("local.properties에 DEBUG_STORE_PASSWORD가 없습니다.")

val debugKeyPassword: String =
    localProperties.getProperty("DEBUG_KEY_PASSWORD")
        ?: error("local.properties에 DEBUG_KEY_PASSWORD가 없습니다.")

val debugKeyAlias: String =
    localProperties.getProperty("DEBUG_KEY_ALIAS") ?: "debugkey"

val kakaoNativeAppKey: String =
    localProperties.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""


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

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug-custom.keystore")
            storePassword = debugStorePassword
            keyAlias = debugKeyAlias
            keyPassword = debugKeyPassword
        }
    }

    defaultConfig {
        applicationId = "com.jeong.runninggoaltracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.jeong.runninggoaltracker.app.HiltTestRunner"

        defaultConfig.manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeAppKey
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
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
    // Project modules
    implementation(project(":data"))
    implementation(project(":shared:designsystem"))
    implementation(project(":shared:navigation"))
    implementation(project(":feature:home"))
    implementation(project(":feature:goal"))
    implementation(project(":feature:record"))
    implementation(project(":feature:reminder"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:ai-coach"))
    implementation(project(":feature:auth"))

    // Core / lifecycle / activity / viewmodel-compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kakao.sdk.user)

    // Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Compose UI
    implementation(libs.bundles.androidx.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Firebase
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom))

    // Unit test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)

    // Android test
    androidTestImplementation(libs.bundles.androidx.compose.test)
    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    kspAndroidTest(libs.dagger.hilt.compiler)

    // Debug
    debugImplementation(libs.bundles.androidx.compose.debug)
    debugImplementation(libs.leakcanary.android)
}
