plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.raju.spawearable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.raju.spawearable"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

    }

    packagingOptions {
        resources.excludes += "META-INF/INDEX.LIST"
        resources.excludes += "META-INF/DEPENDENCIES"
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
        compose = true
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.material3.android)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.media3.common.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation ("androidx.work:work-runtime-ktx:2.9.0")
//    implementation ("androidx.compose.foundation:foundation:1.6.0")
//    implementation ("androidx.compose.material:material:1.6.0")
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-service:2.6.2")
    implementation ("com.google.android.gms:play-services-location:21.2.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    // Wear-specific navigation (optional)
    implementation ("androidx.wear.compose:compose-navigation:1.3.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    // Accompanist Navigation Animation (optional)
    implementation ("com.google.accompanist:accompanist-navigation-animation:0.34.0")
}