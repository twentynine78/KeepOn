plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "fr.twentynine.keepon"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.twentynine.keepon"
        minSdk = 28
        targetSdk = 36
        versionCode = 24
        versionName = "2.0.4"

        vectorDrawables {
            useSupportLibrary = true
        }

        val coilVersionFromToml: String = libs.versions.coilVersion.get()
        resValue("string", "coil_version", coilVersionFromToml)
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            isDebuggable = true
            isJniDebuggable = true
            isPseudoLocalesEnabled = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
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
    jvmToolchain(JavaVersion.VERSION_21.majorVersion.toInt())
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.window.size)
    implementation(libs.compose.material3.adaptive.navigation)
    ksp(libs.com.google.dagger.hilt.compiler)
    implementation(libs.com.google.android.material)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.com.google.dagger.hilt.android)
    implementation(libs.androidx.hilt.work)
    implementation(libs.coil.compose)
    implementation(libs.coil.core)
    implementation(libs.org.jetbrains.kotlinx.collections.immutable)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
}
