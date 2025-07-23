import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    id ("com.google.gms.google-services")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

android {
        namespace = "com.example.myapp"
        compileSdk = 36
        buildFeatures {
            buildConfig=true

    }

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    // Jetpack Compose

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("1.8")
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

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}



dependencies {
    implementation(platform(libs.firebase.bom))


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.mpandroidchart)
    implementation(libs.generativeai)
    implementation(libs.guava)
    implementation(libs.reactive.streams)
    implementation(libs.lifecycle.viewmodel)
    implementation (libs.lifecycle.livedata)
    implementation (libs.play.services.maps.v1802)
    implementation (libs.places)
    implementation (libs.volley)
    implementation (libs.firebase.auth)
    implementation (libs.ycharts)

    // Jetpack Compose

    // Jetpack Compose BOM (manages versions)
    implementation(platform(libs.compose.bom))

    // Core UI components
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)

    // Material3
    implementation(libs.material3)

    // Activity integration
    implementation(libs.activity.compose)

    // Optional: for previews
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // Navigation
    implementation(libs.navigation.compose)


}
