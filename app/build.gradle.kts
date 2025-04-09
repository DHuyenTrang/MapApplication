plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mapapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mapapplication"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    implementation(fileTree(mapOf(
//        "dir" to "libs",
//        "include" to listOf("*.aar", "*.jar"),
//        "exclude" to listOf("*mock*.jar")
//    )))
    // gg service
    implementation ("com.google.android.gms:play-services-location:21.3.0")

    // material design
    implementation ("com.google.android.material:material:1.11.0")

    //map4d
    implementation ("vn.map4d:Map4dTypes:1.1.0")
    implementation ("vn.map4d:Map4dMap:2.6.4")

    // koin
    implementation ("io.insert-koin:koin-android:3.5.0")
    implementation ("io.insert-koin:koin-androidx-navigation:3.5.0")

    // retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // Gson Converter
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2") // Logging
}