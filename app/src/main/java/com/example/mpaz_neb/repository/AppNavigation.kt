package com.example.mpaz_neb.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Pantallas en com.example.mpaz_neb.screens
import com.example.mpaz_neb.screens.HomeScreen
import com.example.mpaz_neb.screens.StudentCodeScreen
import com.example.mpaz_neb.screens.TeacherLoginScreen

// Pantallas en com.example.mpaz_neb.ui.theme.screens
import com.example.mpaz_neb.screens.LecturaScreen
import com.example.mpaz_neb.ui.theme.screens.MatematicaScreen

import com.example.mpaz_neb.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.TeacherLogin.route) {
            TeacherLoginScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.StudentCode.route) {
            StudentCodeScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.Lectura.route) {
            LecturaScreen(
                navController = navController,
                viewModel = viewModel

            )
        }

        // Módulo de Matemática
        composable(Screen.Matematica.route) {
            MatematicaScreen(
                navController = navController,
                viewModel = viewModel,
                onNavigate = TODO()
            )
        }
    }
}

