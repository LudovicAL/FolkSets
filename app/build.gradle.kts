plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.bandito.folksets"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bandito.folksets"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.commons.lang3)
    implementation(libs.commons.io)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.legacy.support.v4)
    implementation(libs.splash.screen)
    testImplementation(libs.junit)
    androidTestImplementation(libs.assertj)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}