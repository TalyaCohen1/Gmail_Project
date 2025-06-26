import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val defaultApiUrl = "http://10.0.2.2:3000/"

fun getApiUrl(): String {
    var url = defaultApiUrl
    val propertiesFile = rootProject.file("local.properties")
    if (!propertiesFile.exists()) {
        println("No local.properties file found. Using default API URL: $url")
        return url
    }
    // Correctly read from properties file
    val properties = Properties()
    propertiesFile.inputStream().use { properties.load(it) }
    val serverUrl = properties.getProperty("server.url")
    if (!serverUrl.isNullOrEmpty()) {
        url = serverUrl.trim().removeSurrounding("\"")
        println("Using server.url from local.properties: $url")
    } else {
        println("server.url not found or is empty in local.properties. Using default API URL: $url")
    }
    return url
}


val API_URL = getApiUrl()

android {
    namespace = "com.example.android_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.android_app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            // Both dev and prod should use API_URL
            buildConfigField("String", "SERVER_URL", "\"$API_URL\"")
        }
        create("prod") {
            dimension = "environment"
            // Both dev and prod should use API_URL
            buildConfigField("String", "SERVER_URL", "\"$API_URL\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}