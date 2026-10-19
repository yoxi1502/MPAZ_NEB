package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.mpaz_neb.navigation.Screen
import com.example.mpaz_neb.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    // Estado para la carga inicial
    var assignedModule by mutableStateOf("Lectura")
    var assignedLevel by mutableStateOf("2.° Año Básico")
    var assignedTopic by mutableStateOf("Comprensión de lectura - Nivel 1")
    var assignedStudent by mutableStateOf("Estudiante 04 - Aula B")
    var generatedSessionCode by mutableStateOf("MPAZ-402")
        private set

    fun updateAssignment(module: String, level: String, topic: String, student: String) {
        assignedModule = module
        assignedLevel = level
        assignedTopic = topic
        assignedStudent = student
        // Genera un código temporal dinámico o mantiene la convención
        val randomNum = (100..999).random()
        generatedSessionCode = "MPAZ-$randomNum"
    }
    // Agrega esto dentro de tu MainViewModel class:

    var teacherEmail by mutableStateOf("")
        private set

    var teacherPassword by mutableStateOf("")
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    fun onEmailChanged(email: String) {
        teacherEmail = email
        loginError = null
    }

    fun onPasswordChanged(password: String) {
        teacherPassword = password
        loginError = null
    }

    fun loginAsistente(onLoginSuccess: () -> Unit) {
        // Validación ficticia de campos para la cuenta de prueba del asistente[cite: 26]
        if (teacherEmail.isNotBlank() && teacherPassword.isNotBlank()) {
            loginError = null
            onLoginSuccess() // Activa la navegación
        } else {
            loginError = "Por favor, ingresa correo y contraseña."
        }
    }

    fun setNavController(navController: NavHostController) {}
    fun navigateTo(screen: Screen) {
        // Lógica para enviar el NavigationEvent.NavigateTo(screen)
    }
}