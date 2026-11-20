plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.mpaz_neb"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.mpaz_neb"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // IP del computador donde corren los microservicios.
        // 10.0.2.2 es el "localhost" del PC visto desde el emulador de Android.
        // Para un celular físico: agregar mpaz.servidor.host=<IP del PC> en gradle.properties.
        val hostServidor = providers.gradleProperty("mpaz.servidor.host").getOrElse("10.0.2.2")
        buildConfigField("String", "HOST_SERVIDOR", "\"$hostServidor\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
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
}

dependencies {
    // Compose + Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Arquitectura (MVVM, navegación y corrutinas)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Persistencia local
    implementation(libs.androidx.datastore.preferences)

    // Consumo de API REST (Retrofit + conversor JSON Gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    // Muestra en Logcat cada petición y respuesta HTTP (útil para la demostración)
    implementation(libs.okhttp.logging)

    // Cámara (CameraX)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.compose)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
