plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    kotlin("kapt")
}

android {
    namespace = "com.example.appgreenflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.appgreenflow"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = false
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    //Có note từng implementation
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.activity)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:24.0.1")

    // CardView cho cards
    implementation("androidx.cardview:cardview:1.0.0")

    // OSMDroid
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    // Thêm để hỗ trợ tile offline
    implementation("org.osmdroid:osmdroid-bonuspack:6.1.18")

    // Dynamic images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // FCM
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.3")

    // Existing
    implementation("org.osmdroid:osmdroid-android:6.1.18")  // OSM maps
    implementation("org.osmdroid:osmdroid-thirdparty:6.1.18")  // Routing support
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")  // MVVM
    implementation("androidx.viewbinding:viewbinding:1.5.11")  // ViewBinding
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")  // Firestore UI helpers
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")  // FCM
    implementation("androidx.paging:paging-runtime:3.3.2")  // Pagination
    // Room for offline cache (optional)
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("androidx.recyclerview:recyclerview:1.3.2")  // Base
    implementation("androidx.paging:paging-runtime:3.3.2")  // Paging 3
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")  // Cho Paging với Firestore

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("androidx.activity:activity-ktx:1.9.2")
}
