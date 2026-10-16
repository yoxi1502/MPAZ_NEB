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
    var isLoading by mutableStateOf(true)
        private set

    // Estado del botón / modo activo recuperado desde DataStore
    var isModoEspecialActive by mutableStateOf(false)
        private set

    // Estado para feedback animado
    var showSuccessMessage by mutableStateOf(false)
        private set

    init {
        // Carga inicial y escucha activa de DataStore (Parte 2)
        viewModelScope.launch {
            delay(1000) // Carga simulada (Parte 3)
            repository.modoEspecialFlow.collect { guardado ->
                isModoEspecialActive = guardado
                isLoading = false
            }
        }
    }

    // Función que altera el estado y lo guarda persistentemente (Parte 2)
    fun toggleModoEspecial(activo: Boolean) {
        viewModelScope.launch {
            isModoEspecialActive = activo

            // Persistencia en DataStore Preferences
            repository.guardarModoEspecial(activo)

            // Feedback visual
            showSuccessMessage = true
            delay(2000)
            showSuccessMessage = false
        }
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