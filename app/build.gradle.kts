import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Injected by CI from the pushed tag (vX.Y.Z); local builds default to 1.0.0 / 1.
val appVersionName: String = providers.gradleProperty("appVersionName").getOrElse("1.0.0")
val appVersionCode: Int = providers.gradleProperty("appVersionCode").map(String::toInt).getOrElse(1)

// Release signing comes ONLY from environment variables (CI maps them from GitHub secrets).
// None set  -> release APK is left unsigned (never silently signed with the debug key).
// All set   -> release APK is signed with that keystore.
// Some set  -> hard failure, so a half-configured secret set can't produce a surprise artifact.
val signingVars = listOf("PE_KEYSTORE_FILE", "PE_KEYSTORE_PASSWORD", "PE_KEY_ALIAS", "PE_KEY_PASSWORD")
val signingEnv: Map<String, String> = signingVars.associateWith { System.getenv(it).orEmpty() }
val signingProvided = signingEnv.filterValues { it.isNotBlank() }.keys
val releaseSigning = signingProvided.isNotEmpty()
if (releaseSigning && signingProvided.size != signingVars.size) {
    throw GradleException(
        "Incomplete release signing configuration. Missing: ${(signingVars - signingProvided).joinToString()}. " +
            "Set all of ${signingVars.joinToString()} or none."
    )
}
if (releaseSigning && !file(signingEnv.getValue("PE_KEYSTORE_FILE")).isFile) {
    throw GradleException("PE_KEYSTORE_FILE does not point to an existing file: ${signingEnv["PE_KEYSTORE_FILE"]}")
}

android {
    namespace = "com.ak7shuvo.personalexpense"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ak7shuvo.personalexpense"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (releaseSigning) {
            create("release") {
                storeFile = file(signingEnv.getValue("PE_KEYSTORE_FILE"))
                storePassword = signingEnv.getValue("PE_KEYSTORE_PASSWORD")
                keyAlias = signingEnv.getValue("PE_KEY_ALIAS")
                keyPassword = signingEnv.getValue("PE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (releaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }

    testOptions { unitTests.isIncludeAndroidResources = true }
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons.core)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
