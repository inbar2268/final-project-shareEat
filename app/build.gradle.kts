plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs)
    id("kotlin-kapt")
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.shareeat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.shareeat"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildConfigField("String", "CLOUD_NAME", "\"${project.properties["CLOUD_NAME"] ?: ""}\"")
        buildConfigField("String", "API_KEY", "\"${project.properties["API_KEY"] ?: ""}\"")
        buildConfigField("String", "API_SECRET", "\"${project.properties["API_SECRET"] ?: ""}\"")
        buildConfigField(
            "String",
            "TMDB_BASE_URL",
            "\"${project.properties["TMDB_BASE_URL"] ?: ""}\""
        )
        buildConfigField(
            "String",
            "TMDB_POSTER_BASE_URL",
            "\"${project.properties["TMDB_POSTER_BASE_URL"] ?: ""}\""
        )
        buildConfigField(
            "String",
            "TMDB_ACCESS_TOKEN",
            "\"${project.properties["TMDB_ACCESS_TOKEN"] ?: ""}\""
        )

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
        buildConfig = true
    }
}

dependencies {


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.converter.gson)

    implementation(libs.androidx.swiperefreshlayout)
    implementation(platform(libs.firebase.bom.v33100))
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.converter.gson)

    implementation (libs.geofire.android)
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.room.runtime)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.picasso)
    implementation(libs.cloudinary.android)
    implementation(libs.firebase.auth.ktx.v2230)


    implementation(libs.cloudinary.android.v231)
    implementation(libs.play.services.location)

    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.security.crypto)
    implementation(libs.circleimageview)
    implementation(libs.glide)



}