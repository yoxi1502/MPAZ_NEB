plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    jacoco
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
        debug {
            // Mide la cobertura de las pruebas unitarias (JaCoCo):
            // ./gradlew createDebugUnitTestCoverageReport
            enableUnitTestCoverage = true
        }
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

    // Carga de imágenes desde internet (sprite del Pokémon)
    implementation(libs.coil.compose)

    // Cámara (CameraX)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.compose)

    // Pruebas unitarias: JUnit, MockK (simula repositorios y APIs) y corrutinas de prueba
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

/**
 * Reporte de cobertura SOLO de la lógica de la app: ViewModels, repositorios,
 * modelos, DTO/mapeos y validaciones. Se dejan fuera las pantallas Compose y lo que
 * necesita un dispositivo real (DataStore y la configuración de Retrofit), que se
 * prueban ejecutando la app en el emulador.
 *
 * Ejecutar: ./gradlew reporteCoberturaLogica
 * Resultado: app/build/reports/jacoco/reporteCoberturaLogica/html/index.html
 */
tasks.register<JacocoReport>("reporteCoberturaLogica") {
    group = "verification"
    description = "Cobertura de las pruebas unitarias sobre la lógica de la app."
    dependsOn("testDebugUnitTest")

    val clases = layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
    classDirectories.setFrom(
        clases.map { carpeta ->
            fileTree(carpeta) {
                include(
                    "com/example/mpaz_neb/viewmodel/**",
                    "com/example/mpaz_neb/repository/**",
                    "com/example/mpaz_neb/model/**",
                    "com/example/mpaz_neb/data/**",
                    "com/example/mpaz_neb/validacion/**"
                )
                exclude(
                    "com/example/mpaz_neb/data/local/**",
                    "com/example/mpaz_neb/data/remote/RetrofitInstance*"
                )
            }
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    )
    reports {
        html.required = true
        xml.required = true
    }
}
