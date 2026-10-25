package com.example.mpaz_neb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.mpaz_neb.navigation.AppNavigation
import com.example.mpaz_neb.ui.theme.MPAZ_NEBTheme
import com.example.mpaz_neb.viewmodel.SesionUsuarioViewModel

class MainActivity : ComponentActivity() {

    // ViewModel compartido con la sesión del equipo pedagógico (ligado a la Activity)
    private val sesionUsuarioViewModel: SesionUsuarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MPAZ_NEBTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        navController = rememberNavController(),
                        sesionUsuarioViewModel = sesionUsuarioViewModel
                    )
                }
            }
        }
    }
}
