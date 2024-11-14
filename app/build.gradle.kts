plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "kr.co.kimberly.wma"
    compileSdk = 34

    defaultConfig {
        applicationId = "kr.co.kimberly.wma"
        minSdk = 28
        targetSdk = 34
        versionCode = 24110401
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // 권한
    implementation("io.github.ParkSangGwon:tedpermission-normal:3.3.0")

    // 이미지 전체화면
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    //gson
    implementation("com.google.code.gson:gson:2.10.1")

    // TSC 프린터
    implementation(files("libs/bluetooth.jar"))
    // SM-F711N 모델 스캔 라이브러리
    implementation(files("libs/device.sdk.jar"))
    implementation(files("libs/kdclib.jar"))

    //lottie 라이브러리
    implementation ("com.airbnb.android:lottie:3.7.0")

    //네트워크 통신 및 json파싱 라이브러리
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.6.1")
    implementation ("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    //retrofit2 로그 확인을 위한 라이브러리
    implementation ("com.squareup.okhttp3:okhttp:4.9.2")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.2")

    //multidex
    implementation ("com.android.support:multidex:1.0.3")

    //코루틴
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    //이미지 로딩
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    // ViewModel and LiveData
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation ("androidx.activity:activity-ktx:1.9.3")
}