plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.lifegrow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lifegrow"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }

}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.appcompat)
    implementation(libs.material)
    // MPAndroidChart for charts (uncommented)
    //implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
   // implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //implementation ("com.squareup.okhttp3:okhttp:4.9.0")
   // implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("com.joestelmach:natty:0.13")






    // Glide for images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // WorkManager (only include once)
    implementation("androidx.work:work-runtime:2.8.1")

    // Firebase Firestore (if not using the version catalog for it, comment one of these)
    implementation("com.google.firebase:firebase-firestore:24.10.0")

    // Room (only annotationProcessor for room-compiler is needed if using annotationProcessor)
    implementation("androidx.room:room-runtime:2.5.0")
    annotationProcessor("androidx.room:room-compiler:2.5.0")

    // Guava for ListenableFuture
    implementation("com.google.guava:guava:31.0.1-android")

    implementation("com.prolificinteractive:material-calendarview:1.4.3")

    // Kizitonwose Calendar View (if you plan to use it; you can choose one calendar library)

    implementation("com.kizitonwose.calendar:view:2.3.0")
    // Horizontal calendar (lightweight & scrollable)



    implementation("androidx.core:core:1.13.0") {
        exclude(group = "com.android.support", module = "support-compat")
    }




}
