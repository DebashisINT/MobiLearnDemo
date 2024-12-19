plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.breezemobilearndemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.breezemobilearndemo"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.1"

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
        viewBinding = true
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.sdp.android)
    implementation (libs.preferenceholder)
    implementation (libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    implementation (libs.androidx.recyclerview)
    implementation (libs.lottie)
    implementation (libs.exoplayer)
    implementation (libs.androidx.viewpager2)
    implementation (libs.retrofit)
    implementation (libs.adapter.rxjava2)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor.v380)
    implementation (libs.rxjava)
    implementation (libs.rxandroid)
    implementation (libs.commons.lang3)
    implementation (libs.materialish.progress)
    implementation (libs.circleimageview)
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.rxjava2)
    implementation (libs.androidx.room.common)
    kapt(libs.androidx.room.compiler)

    //Firebase
    implementation (libs.firebase.core)
    implementation (libs.firebase.messaging)
    implementation (libs.firebase.analytics) // or other Firebase libraries you need
    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.easypermissions.ktx)

}