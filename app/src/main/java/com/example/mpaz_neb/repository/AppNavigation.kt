package com.example.mpaz_neb.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mpaz_neb.screens.AudioScreen
import com.example.mpaz_neb.screens.CamaraScreen
import com.example.mpaz_neb.screens.HomeScreen
import com.example.mpaz_neb.screens.LecturaScreen
import com.example.mpaz_neb.screens.ProgresoScreen
import com.example.mpaz_neb.ui.theme.screens.AssignActivityScreen
import com.example.mpaz_neb.ui.theme.screens.MatematicaScreen
import com.example.mpaz_neb.ui.theme.screens.SesionEnCursoScreen
import com.example.mpaz_neb.ui.theme.screens.StudentCodeScreen
import com.example.mpaz_neb.ui.theme.screens.TeacherLoginScreen
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

        composable(Screen.AssignActivity.route) {
            AssignActivityScreen(
                navController = navController
            )
        }

        composable(Screen.SesionEnCurso.route) {
            SesionEnCursoScreen(
                navController = navController
            )
        }

        composable(Screen.Lectura.route) {
            LecturaScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.Matematica.route) {
            MatematicaScreen(
                navController = navController,
                viewModel = viewModel,
                onNavigate = { evento ->
                    when (evento) {
                        is NavigationEvent.NavigateTo -> {
                            navController.navigate(evento.route.route) {
                                launchSingleTop = evento.singleTop

                                evento.popupToRoute?.let { destino ->
                                    popUpTo(destino.route) {
                                        inclusive = evento.inclusive
                                    }
                                }
                            }
                        }

                        NavigationEvent.PopBackStack -> {
                            navController.popBackStack()
                        }

                        NavigationEvent.NavigateUp -> {
                            navController.navigateUp()
                        }
                    }
                }
            )
        }

        composable(Screen.Progreso.route) {
            ProgresoScreen(
                navController = navController
            )
        }

        composable(Screen.Camara.route) {
            CamaraScreen(
                navController = navController
            )
        }

        composable(Screen.Audio.route) {
            AudioScreen(
                navController = navController
            )
        }
    }
}
