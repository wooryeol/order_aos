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
        versionCode = 24030501
        versionName = "1.0.0"

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
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

    //lottie 라이브러리
    implementation ("com.airbnb.android:lottie:3.7.0")
}