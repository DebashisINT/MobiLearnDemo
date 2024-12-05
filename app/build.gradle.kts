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
        versionName = "5.0.1"

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
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation ("com.marcinmoskala.PreferenceHolder:preferenceholder:1.51")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("com.airbnb.android:lottie:6.4.1")
    implementation ("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:3.8.0")
    implementation ("io.reactivex.rxjava2:rxjava:2.1.6")
    implementation ("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation ("org.apache.commons:commons-lang3:3.12.0")
    implementation ("com.pnikosis:materialish-progress:1.7")
    implementation ("de.hdodenhof:circleimageview:2.2.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.room:room-rxjava2:2.6.1")
    implementation ("androidx.room:room-common:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    //Firebase
    implementation ("com.google.firebase:firebase-core:21.1.1")
    implementation ("com.google.firebase:firebase-messaging:23.1.0")
    implementation ("com.google.firebase:firebase-analytics:21.0.0") // or other Firebase libraries you need
    implementation("com.google.auth:google-auth-library-oauth2-http:1.3.0")

}