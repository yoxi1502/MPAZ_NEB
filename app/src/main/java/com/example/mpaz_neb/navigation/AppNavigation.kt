package com.example.mpaz_neb.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mpaz_neb.model.PerfilUsuario
import com.example.mpaz_neb.model.UsuarioSesion
import com.example.mpaz_neb.model.UsuarioSesionUiState
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
import com.example.mpaz_neb.viewmodel.SesionUsuarioViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    sesionUsuarioViewModel: SesionUsuarioViewModel
) {
    val sesionUsuario by sesionUsuarioViewModel.uiState.collectAsState()

    val cerrarSesion: () -> Unit = {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
            launchSingleTop = true
        }
        sesionUsuarioViewModel.cerrarSesion()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
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

        // ---- Rutas protegidas: solo equipo pedagógico con sesión iniciada ----

        composable(Screen.AssignActivity.route) {
            RutaProtegida(sesionUsuario, navController) { usuario ->
                AssignActivityScreen(
                    navController = navController,
                    usuario = usuario,
                    onCerrarSesion = cerrarSesion
                )
            }
        }

        composable(Screen.SesionEnCurso.route) {
            RutaProtegida(sesionUsuario, navController) {
                SesionEnCursoScreen(navController = navController)
            }
        }

        composable(Screen.Progreso.route) {
            RutaProtegida(sesionUsuario, navController) {
                ProgresoScreen(navController = navController)
            }
        }

        // ---- Actividades del estudiante ----

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

                        NavigationEvent.PopBackStack -> navController.popBackStack()
                        NavigationEvent.NavigateUp -> navController.navigateUp()
                    }
                }
            )
        }

        composable(Screen.Camara.route) {
            CamaraScreen(navController = navController)
        }

        composable(Screen.Audio.route) {
            AudioScreen(navController = navController)
        }
    }
}

/**
 * Muestra el contenido solo si hay un usuario del equipo pedagógico con un perfil permitido.
 * Si no lo hay, redirige al login. Mientras la pantalla sale tras cerrar sesión,
 * se mantiene el último usuario para evitar una redirección extra.
 */
@Composable
private fun RutaProtegida(
    estado: UsuarioSesionUiState,
    navController: NavHostController,
    perfilesPermitidos: Set<PerfilUsuario> = PerfilUsuario.entries.toSet(),
    contenido: @Composable (UsuarioSesion) -> Unit
) {
    val ultimoUsuario = remember { mutableStateOf<UsuarioSesion?>(null) }
    SideEffect {
        if (estado.usuario != null) ultimoUsuario.value = estado.usuario
    }
    val usuario = estado.usuario ?: ultimoUsuario.value

    when {
        estado.cargando -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        usuario == null || usuario.perfil !in perfilesPermitidos -> {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.TeacherLogin.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            }
        }

        else -> contenido(usuario)
    }
}
