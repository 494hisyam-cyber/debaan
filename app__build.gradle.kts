plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Diisi otomatis oleh GitHub Actions (-PbuildNumber, -PgithubRepo).
val buildNumber = (project.findProperty("buildNumber") as String?)?.toIntOrNull() ?: 1
val githubRepo = (project.findProperty("githubRepo") as String?) ?: "USERNAME/debaan"

android {
    namespace = "com.debaan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.debaan"
        minSdk = 26
        targetSdk = 34
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
        buildConfigField("String", "GITHUB_REPO", "\"$githubRepo\"")
    }

    // Kunci tanda tangan dibaca dari variabel lingkungan (GitHub Secrets), tidak disimpan di repo.
    val ksPassword: String? = System.getenv("KEYSTORE_PASSWORD")
    signingConfigs {
        if (ksPassword != null) {
            create("release") {
                storeFile = file(System.getenv("KEYSTORE_PATH") ?: "debaan.jks")
                storePassword = ksPassword
                keyAlias = System.getenv("KEY_ALIAS") ?: "debaan"
                keyPassword = System.getenv("KEY_PASSWORD") ?: ksPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfigs.findByName("release")?.let { signingConfig = it }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
