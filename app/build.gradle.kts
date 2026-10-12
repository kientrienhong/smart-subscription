import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.smartsubscription"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.smartsubscription"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "0.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Invoice scanning: ML Kit reads the text on the device, Gemini Nano (where the phone has it) structures it.
    implementation(libs.mlkit.text.recognition)
    // Other scripts come through Google Play services: nothing in the APK, the model downloads when the person's languages need it.
    implementation(libs.mlkit.text.recognition.chinese)
    implementation(libs.mlkit.text.recognition.japanese)
    implementation(libs.mlkit.text.recognition.korean)
    implementation(libs.mlkit.text.recognition.devanagari)
    implementation(libs.mlkit.genai.prompt)
    // Translates non-English invoices so the pattern parser can fill fields it has no local keywords for.
    implementation(libs.mlkit.language.id)
    implementation(libs.mlkit.translate)

    testImplementation(libs.junit)
    // The JVM's stub org.json throws in unit tests; the real one lets the model-reply parsing be tested.
    testImplementation(libs.org.json)
}
