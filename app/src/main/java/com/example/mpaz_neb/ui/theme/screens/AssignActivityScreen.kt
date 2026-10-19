package com.example.mpaz_neb.ui.theme.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavHostController
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.navigation.Screen
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.viewmodel.SesionAulaViewModel

@Composable
fun AssignActivityScreen(
    navController: NavHostController,
    sesionViewModel: SesionAulaViewModel = obtenerViewModel()
) {
    val estado by sesionViewModel.uiState.collectAsState()
    val sesionAbierta = estado.sesion?.estado?.let { it != EstadoSesion.FINALIZADA } == true

    BackHandler(enabled = estado.guardando) { }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    enabled = !estado.guardando,
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) { Text("Cerrar acceso docente") }

                TextButton(
                    enabled = !estado.guardando,
                    onClick = {
                        navController.navigate(Screen.Progreso.route) { launchSingleTop = true }
                    }
                ) { Text("Resultados") }
            }

            Text(
                text = "Asignar actividad",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Elige la práctica y el estudiante que la realizará en este dispositivo.")

            if (estado.cargando) {
                CircularProgressIndicator()
            }

            estado.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(
                    onClick = sesionViewModel::cargarSesion,
                    enabled = !estado.guardando
                ) { Text("Reintentar lectura") }
            }

            if (sesionAbierta) {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Ya hay una sesión abierta", fontWeight = FontWeight.Bold)
                        Text("${estado.sesion?.estudiante} · ${estado.sesion?.modulo?.etiqueta}")
                        Text("Abre la sesión para continuar o finalizarla antes de crear otra.")
                        Button(
                            onClick = {
                                navController.navigate(Screen.SesionEnCurso.route) { launchSingleTop = true }
                            },
                            enabled = !estado.guardando
                        ) { Text("Abrir sesión actual") }
                    }
                }
            }

            val habilitado = !estado.cargando && !estado.guardando && !sesionAbierta

            Text("Módulo", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModuloActividad.entries.forEach { modulo ->
                    FilterChip(
                        selected = estado.modulo == modulo,
                        onClick = { sesionViewModel.seleccionarModulo(modulo) },
                        label = { Text(modulo.etiqueta) },
                        enabled = habilitado,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SelectorAsignacion(
                titulo = "Nivel / Curso",
                valor = estado.curso,
                opciones = SesionAulaRepository.CURSOS,
                habilitado = habilitado,
                onSeleccion = sesionViewModel::seleccionarCurso
            )

            OutlinedTextField(
                value = estado.modulo.contenido,
                onValueChange = {},
                readOnly = true,
                label = { Text("Contenido disponible") },
                modifier = Modifier.fillMaxWidth()
            )

            SelectorAsignacion(
                titulo = "Estudiante asignado",
                valor = estado.estudiante,
                opciones = SesionAulaRepository.ESTUDIANTES,
                habilitado = habilitado,
                onSeleccion = sesionViewModel::seleccionarEstudiante
            )

            Button(
                enabled = habilitado,
                onClick = {
                    sesionViewModel.generarCodigo {
                        navController.navigate(Screen.SesionEnCurso.route) { launchSingleTop = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(if (estado.guardando) "Guardando sesión…" else "Generar código de sesión")
            }

            Text(
                "Los estudiantes de la lista son alias de demostración.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorAsignacion(
    titulo: String,
    valor: String,
    opciones: List<String>,
    habilitado: Boolean,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido && habilitado,
        onExpandedChange = { if (habilitado) expandido = !expandido }
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(titulo) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido && habilitado,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}
