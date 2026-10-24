package com.example.mpaz_neb.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mpaz_neb.ui.screens.AssignActivityScreen
import com.example.mpaz_neb.ui.screens.AudioScreen
import com.example.mpaz_neb.ui.screens.CamaraScreen
import com.example.mpaz_neb.ui.screens.HomeScreen
import com.example.mpaz_neb.ui.screens.LecturaScreen
import com.example.mpaz_neb.ui.screens.MatematicaScreen
import com.example.mpaz_neb.ui.screens.ProgresoScreen
import com.example.mpaz_neb.ui.screens.SesionEnCursoScreen
import com.example.mpaz_neb.ui.screens.StudentCodeScreen
import com.example.mpaz_neb.ui.screens.TeacherLoginScreen

private const val DURACION_TRANSICION_MS = 350

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    // Transiciones animadas entre pantallas: deslizamiento lateral con desvanecido.
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(DURACION_TRANSICION_MS)
            ) + fadeIn(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(DURACION_TRANSICION_MS)
            ) + fadeOut(animationSpec = tween(DURACION_TRANSICION_MS))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.TeacherLogin.route) {
            TeacherLoginScreen(navController = navController)
        }

        composable(Screen.StudentCode.route) {
            StudentCodeScreen(navController = navController)
        }

        composable(Screen.AssignActivity.route) {
            AssignActivityScreen(navController = navController)
        }

        composable(Screen.SesionEnCurso.route) {
            SesionEnCursoScreen(navController = navController)
        }

        composable(Screen.Lectura.route) {
            LecturaScreen(navController = navController)
        }

        composable(Screen.Matematica.route) {
            MatematicaScreen(
                navController = navController,
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
            ProgresoScreen(navController = navController)
        }

        composable(Screen.Camara.route) {
            CamaraScreen(navController = navController)
        }

        composable(Screen.Audio.route) {
            AudioScreen(navController = navController)
        }
    }
}
