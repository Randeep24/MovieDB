plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.jetbrains.kotlin.android)
        alias(libs.plugins.jetbrains.kotlin.kapt)
        alias(libs.plugins.gradle.hilt)
}

android {
        namespace = "com.randeep.moviedb"
        compileSdk = 34

        defaultConfig {
                applicationId = "com.randeep.moviedb"
                minSdk = 24
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
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
                jvmTarget = "17"
        }

        buildFeatures {
                viewBinding = true
        }
}

dependencies {

        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)

        implementation(libs.hilt.android)
        kapt(libs.hilt.compiler)

        implementation(libs.squareup.retrofit)
        implementation(libs.squareup.moshi)
        implementation(libs.squareup.okhttp.interceptor)

        implementation(libs.androidx.navigation.fragment)
        implementation(libs.androidx.navigation.ui)

        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
}