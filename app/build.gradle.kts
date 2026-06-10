import java.net.URL
import java.net.URI
import java.io.File

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.leshcafe.iq"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: "leshcafe123"
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "leshcafe123"
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("downloadFonts") {
    val fontDir = file("src/main/res/font")
    if (!fontDir.exists()) { fontDir.mkdirs() }
    
    val tajawalRegular = file("src/main/res/font/tajawal_regular.ttf")
    val tajawalBold = file("src/main/res/font/tajawal_bold.ttf")
    val tajawalMedium = file("src/main/res/font/tajawal_medium.ttf")

    outputs.files(tajawalRegular, tajawalBold, tajawalMedium)

    doLast {
        val regUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/tajawal/Tajawal-Regular.ttf"
        val boldUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/tajawal/Tajawal-Bold.ttf"
        val medUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/tajawal/Tajawal-Medium.ttf"

        if (!tajawalRegular.exists() || tajawalRegular.length() < 10000) {
            println("Downloading Tajawal-Regular.ttf from $regUrl...")
            URI(regUrl).toURL().openStream().use { input ->
                tajawalRegular.outputStream().use { output ->
                    val copied = input.copyTo(output)
                    println("Downloaded Regular: $copied bytes")
                }
            }
        }
        if (!tajawalBold.exists() || tajawalBold.length() < 10000) {
            println("Downloading Tajawal-Bold.ttf from $boldUrl...")
            URI(boldUrl).toURL().openStream().use { input ->
                tajawalBold.outputStream().use { output ->
                    val copied = input.copyTo(output)
                    println("Downloaded Bold: $copied bytes")
                }
            }
        }
        if (!tajawalMedium.exists() || tajawalMedium.length() < 10000) {
            println("Downloading Tajawal-Medium.ttf from $medUrl...")
            URI(medUrl).toURL().openStream().use { input ->
                tajawalMedium.outputStream().use { output ->
                    val copied = input.copyTo(output)
                    println("Downloaded Medium: $copied bytes")
                }
            }
        }
    }
}

val keystorePathStr = project.rootDir.absolutePath + "/my-upload-key.jks"

tasks.register("generateReleaseKeystore") {
    val path = keystorePathStr
    doLast {
        val keystoreFile = File(path)
        if (!keystoreFile.exists()) {
            println("Generating release keystore at: ${keystoreFile.absolutePath}")
            try {
                val process = ProcessBuilder(
                    "keytool", "-genkeypair", "-v",
                    "-keystore", keystoreFile.absolutePath,
                    "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000",
                    "-alias", "upload",
                    "-storepass", "leshcafe123",
                    "-keypass", "leshcafe123",
                    "-dname", "CN=Lesh Cafe, OU=Android, O=Lesh Cafe, L=Baghdad, S=Baghdad, C=IQ"
                ).inheritIO().start()
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    println("Release keystore generated successfully.")
                } else {
                    println("Failed to generate release keystore. Exit code: $exitCode")
                }
            } catch (e: Exception) {
                println("Failed to generate release keystore via keytool ProcessBuilder: ${e.message}")
            }
        } else {
            println("Release keystore already exists.")
        }
    }
}

tasks.matching { it.name.startsWith("preBuild") }.all {
    dependsOn("downloadFonts", "generateReleaseKeystore")
}

