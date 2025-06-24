import org.gradle.kotlin.dsl.androidTestImplementation
import org.gradle.kotlin.dsl.debugImplementation

plugins {
    alias(libs.plugins.androidApplication)
    //alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.android") version "2.1.0"
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
    namespace = "com.example.pillreminder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pillreminder"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }


}

dependencies {
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose.jvmstubs)
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material:material")
    implementation ("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.tooling)
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-core")
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.adaptive)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime-rxjava2")


    implementation("androidx.core:core:1.15.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-database-ktx")
    implementation ("androidx.media3:media3-common-ktx:1.7.1")

    debugImplementation ("androidx.compose.ui:ui-test-manifest:<2.1.0>")
    testImplementation ("androidx.test:core:1.5.0")
    testImplementation ("org.mockito:mockito-core:5.12.0")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:5.2.1")
    androidTestImplementation("org.mockito:mockito-android:5.11.0")
    testImplementation(kotlin("test"))
    testImplementation ("io.mockk:mockk-android:1.13.3")
    androidTestImplementation ("io.mockk:mockk-android:1.13.3")

}